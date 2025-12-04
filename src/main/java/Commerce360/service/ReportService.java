package Commerce360.service;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import Commerce360.entity.*;
import Commerce360.entity.TransactionType;
import Commerce360.repository.*;
import Commerce360.security.SecurityContextUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportService {

        @Autowired
        private final TransactionRepository transactionRepository;

        @Autowired
        private final InventoryRepository inventoryRepository;

        @Autowired
        private final StoreRepository storeRepository;

        @Autowired
        private final SecurityContextUtil securityContextUtil;

        public ReportService(
                        TransactionRepository transactionRepository,
                        InventoryRepository inventoryRepository,
                        StoreRepository storeRepository,
                        SecurityContextUtil securityContextUtil) {
                this.transactionRepository = transactionRepository;
                this.inventoryRepository = inventoryRepository;
                this.storeRepository = storeRepository;
                this.securityContextUtil = securityContextUtil;
        }

        // @Cacheable(value = "reports", key = "'stock_summary_' + #storeId + '_' +
        // #startDate + '_' + #endDate")
        public Map<String, Object> getStockSummary(UUID storeId, LocalDateTime startDate, LocalDateTime endDate) {
                Store store = storeRepository.findById(storeId)
                                .orElseThrow(() -> new RuntimeException("Store not found"));

                if (!store.getOwner().getId().equals(securityContextUtil.getCurrentUserId())) {
                        throw new RuntimeException("You can only view reports for your own stores");
                }

                List<Transaction> transactions = transactionRepository.findByStoreAndTransactionDateBetween(store,
                                startDate, endDate);

                Map<String, Object> summary = Map.of(
                                "totalStockIn", transactions.stream()
                                                .filter(t -> t.getType() == TransactionType.STOCK_IN)
                                                .mapToInt(Transaction::getQuantity)
                                                .sum(),
                                "totalStockOut", transactions.stream()
                                                .filter(t -> t.getType() == TransactionType.SALE)
                                                .mapToInt(Transaction::getQuantity)
                                                .sum(),
                                "currentStock", inventoryRepository.findByStore(store).stream()
                                                .mapToInt(Inventory::getQuantity)
                                                .sum(),
                                "expiringStock", inventoryRepository.findByExpiryDateBefore(endDate).stream()
                                                .filter(i -> i.getStore().equals(store))
                                                .mapToInt(Inventory::getQuantity)
                                                .sum());

                return summary;
        }

        // @Cacheable(value = "reports", key = "'sales_summary_' + #storeId + '_' +
        // #startDate + '_' + #endDate")
        public Map<String, Object> getSalesSummary(UUID storeId, LocalDateTime startDate, LocalDateTime endDate) {
                Store store = storeRepository.findById(storeId)
                                .orElseThrow(() -> new RuntimeException("Store not found"));

                if (!store.getOwner().getId().equals(securityContextUtil.getCurrentUserId())) {
                        throw new RuntimeException("You can only view reports for your own stores");
                }

                List<Transaction> sales = transactionRepository.findByStoreAndTypeAndTransactionDateBetween(
                                store, TransactionType.SALE, startDate, endDate, Pageable.unpaged()).getContent();

                Map<String, Object> summary = Map.of(
                                "totalSales", sales.stream()
                                                .mapToDouble(Transaction::getTotalAmount)
                                                .sum(),
                                "totalItemsSold", sales.stream()
                                                .mapToInt(Transaction::getQuantity)
                                                .sum(),
                                "averageSalePrice", sales.stream()
                                                .mapToDouble(Transaction::getUnitPrice)
                                                .average()
                                                .orElse(0.0),
                                "topSellingProducts", sales.stream()
                                                .collect(Collectors.groupingBy(
                                                                t -> t.getProduct().getName(),
                                                                Collectors.summingInt(Transaction::getQuantity))));

                return summary;
        }

        // @Cacheable(value = "reports", key = "'purchase_summary_' + #storeId + '_' +
        // #startDate + '_' + #endDate")
        public Map<String, Object> getPurchaseSummary(UUID storeId, LocalDateTime startDate, LocalDateTime endDate) {
                Store store = storeRepository.findById(storeId)
                                .orElseThrow(() -> new RuntimeException("Store not found"));

                if (!store.getOwner().getId().equals(securityContextUtil.getCurrentUserId())) {
                        throw new RuntimeException("You can only view reports for your own stores");
                }

                List<Transaction> purchases = transactionRepository.findByStoreAndTypeAndTransactionDateBetween(
                                store, TransactionType.STOCK_IN, startDate, endDate, Pageable.unpaged()).getContent();

                Map<String, Object> summary = Map.of(
                                "totalPurchases", purchases.stream()
                                                .mapToDouble(Transaction::getTotalAmount)
                                                .sum(),
                                "totalItemsPurchased", purchases.stream()
                                                .mapToInt(Transaction::getQuantity)
                                                .sum(),
                                "averagePurchasePrice", purchases.stream()
                                                .mapToDouble(Transaction::getUnitPrice)
                                                .average()
                                                .orElse(0.0),
                                "topSuppliers", purchases.stream()
                                                .collect(Collectors.groupingBy(
                                                                t -> t.getSupplier().getName(),
                                                                Collectors.summingDouble(
                                                                                Transaction::getTotalAmount))));

                return summary;
        }

        // @Cacheable(value = "reports", key = "'profit_loss_' + #storeId + '_' +
        // #startDate + '_' + #endDate")
        public Map<String, Object> getProfitLossAnalysis(UUID storeId, LocalDateTime startDate, LocalDateTime endDate) {
                Store store = storeRepository.findById(storeId)
                                .orElseThrow(() -> new RuntimeException("Store not found"));

                if (!store.getOwner().getId().equals(securityContextUtil.getCurrentUserId())) {
                        throw new RuntimeException("You can only view reports for your own stores");
                }

                List<Transaction> transactions = transactionRepository.findByStoreAndTransactionDateBetween(store,
                                startDate, endDate);

                double totalSales = transactions.stream()
                                .filter(t -> t.getType() == TransactionType.SALE)
                                .mapToDouble(Transaction::getTotalAmount)
                                .sum();

                double totalPurchases = transactions.stream()
                                .filter(t -> t.getType() == TransactionType.STOCK_IN)
                                .mapToDouble(Transaction::getTotalAmount)
                                .sum();

                double grossProfit = totalSales - totalPurchases;
                double profitMargin = totalSales > 0 ? (grossProfit / totalSales) * 100 : 0;

                Map<String, Object> analysis = Map.of(
                                "totalSales", totalSales,
                                "totalPurchases", totalPurchases,
                                "grossProfit", grossProfit,
                                "profitMargin", profitMargin,
                                "period", Map.of(
                                                "startDate", startDate,
                                                "endDate", endDate));

                return analysis;
        }
}