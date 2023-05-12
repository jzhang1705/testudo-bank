package net.testudobank;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class TestudoBankRepository {
  public static String getCustomerPassword(JdbcTemplate jdbcTemplate, String customerID) {
    String getCustomerPasswordSql = String.format("SELECT Password FROM Passwords WHERE CustomerID='%s';", customerID);
    String customerPassword = jdbcTemplate.queryForObject(getCustomerPasswordSql, String.class);
    return customerPassword;
  }

  public static int getCustomerNumberOfReversals(JdbcTemplate jdbcTemplate, String customerID) {
    String getNumberOfReversalsSql = String.format("SELECT NumFraudReversals FROM Customers WHERE CustomerID='%s';", customerID);
    int numOfReversals = jdbcTemplate.queryForObject(getNumberOfReversalsSql, Integer.class);
    return numOfReversals;
  }

  public static int getCustomerCashBalanceInPennies(JdbcTemplate jdbcTemplate, String customerID) {
    String getUserBalanceSql =  String.format("SELECT Balance FROM Customers WHERE CustomerID='%s';", customerID);
    int userBalanceInPennies = jdbcTemplate.queryForObject(getUserBalanceSql, Integer.class);
    return userBalanceInPennies;
  }

  public static Optional<Double> getCustomerCryptoBalance(JdbcTemplate jdbcTemplate, String customerID, String cryptoName) {
    String getUserCryptoBalanceSql = "SELECT CryptoAmount FROM CryptoHoldings WHERE CustomerID= ? AND CryptoName= ?;";

    try {
      return Optional.ofNullable(jdbcTemplate.queryForObject(getUserCryptoBalanceSql, BigDecimal.class, customerID, cryptoName)).map(BigDecimal::doubleValue);
    } catch (EmptyResultDataAccessException ignored) {
      // user may not have crypto row yet
      return Optional.empty();
    }

  }

  public static int getCustomerOverdraftBalanceInPennies(JdbcTemplate jdbcTemplate, String customerID) {
    String getUserOverdraftBalanceSql = String.format("SELECT OverdraftBalance FROM Customers WHERE CustomerID='%s';", customerID);
    int userOverdraftBalanceInPennies = jdbcTemplate.queryForObject(getUserOverdraftBalanceSql, Integer.class);
    return userOverdraftBalanceInPennies;
  }

  public static List<Map<String,Object>> getRecentTransactions(JdbcTemplate jdbcTemplate, String customerID, int numTransactionsToFetch) {
    String getTransactionHistorySql = String.format("Select * from TransactionHistory WHERE CustomerId='%s' ORDER BY Timestamp DESC LIMIT %d;", customerID, numTransactionsToFetch);
    List<Map<String,Object>> transactionLogs = jdbcTemplate.queryForList(getTransactionHistorySql);
    return transactionLogs;
  }

  public static List<Map<String,Object>> getTransferLogs(JdbcTemplate jdbcTemplate, String customerID, int numTransfersToFetch) {
    String getTransferHistorySql = String.format("Select * from TransferHistory WHERE TransferFrom='%s' OR TransferTo='%s' ORDER BY Timestamp DESC LIMIT %d;", customerID, customerID, numTransfersToFetch);
    List<Map<String,Object>> transferLogs = jdbcTemplate.queryForList(getTransferHistorySql);
    return transferLogs;
  }

  public static List<Map<String,Object>> getOverdraftLogs(JdbcTemplate jdbcTemplate, String customerID){
    String getOverDraftLogsSql = String.format("SELECT * FROM OverdraftLogs WHERE CustomerID='%s';", customerID);
    List<Map<String,Object>> overdraftLogs = jdbcTemplate.queryForList(getOverDraftLogsSql);
    return overdraftLogs;
  }

  public static List<Map<String,Object>> getOverdraftLogs(JdbcTemplate jdbcTemplate, String customerID, String timestamp){
    String getOverDraftLogsSql = String.format("SELECT * FROM OverdraftLogs WHERE CustomerID='%s' AND Timestamp='%s';", customerID, timestamp);
    List<Map<String,Object>> overdraftLogs = jdbcTemplate.queryForList(getOverDraftLogsSql);
    return overdraftLogs;
  }

  public static List<Map<String,Object>> getCryptoLogs(JdbcTemplate jdbcTemplate, String customerID) {
    String getTransferHistorySql = "Select * from CryptoHistory WHERE CustomerID=? ORDER BY Timestamp DESC";
    return jdbcTemplate.queryForList(getTransferHistorySql, customerID);
  }

  public static int getCustomerNumberOfDepositsForInterest(JdbcTemplate jdbcTemplate, String customerID) {
    String getCustomerNumberOfDepositsForInterestSql = String.format("SELECT NumDepositsForInterest FROM Customers WHERE CustomerID='%s';", customerID);
    int numberOfDepositsForInterest = jdbcTemplate.queryForObject(getCustomerNumberOfDepositsForInterestSql, Integer.class);
    return numberOfDepositsForInterest;
  }

  public static void setCustomerNumberOfDepositsForInterest(JdbcTemplate jdbcTemplate, String customerID, int numDepositsForInterest) { 
    String customerInterestDepositsSql = String.format("UPDATE Customers SET NumDepositsForInterest = %d WHERE CustomerID='%s';", numDepositsForInterest, customerID);
    jdbcTemplate.update(customerInterestDepositsSql);
  }

  public static void insertRowToTransactionHistoryTable(JdbcTemplate jdbcTemplate, String customerID, String timestamp, String action, int amtInPennies) {
    String insertRowToTransactionHistorySql = String.format("INSERT INTO TransactionHistory VALUES ('%s', '%s', '%s', %d);",
                                                              customerID,
                                                              timestamp,
                                                              action,
                                                              amtInPennies);
    jdbcTemplate.update(insertRowToTransactionHistorySql);
  }

  public static void insertRowToOverdraftLogsTable(JdbcTemplate jdbcTemplate, String customerID, String timestamp, int depositAmtIntPennies, int oldOverdraftBalanceInPennies, int newOverdraftBalanceInPennies) {
    String insertRowToOverdraftLogsSql = String.format("INSERT INTO OverdraftLogs VALUES ('%s', '%s', %d, %d, %d);", 
                                                        customerID,
                                                        timestamp,
                                                        depositAmtIntPennies,
                                                        oldOverdraftBalanceInPennies,
                                                        newOverdraftBalanceInPennies);
    jdbcTemplate.update(insertRowToOverdraftLogsSql);
  }

  public static void setCustomerNumFraudReversals(JdbcTemplate jdbcTemplate, String customerID, int newNumFraudReversals) {
    String numOfReversalsUpdateSql = String.format("UPDATE Customers SET NumFraudReversals = %d WHERE CustomerID='%s';", newNumFraudReversals, customerID);
    jdbcTemplate.update(numOfReversalsUpdateSql);
  }

  public static void setCustomerOverdraftBalance(JdbcTemplate jdbcTemplate, String customerID, int newOverdraftBalanceInPennies) {
    String overdraftBalanceUpdateSql = String.format("UPDATE Customers SET OverdraftBalance = %d WHERE CustomerID='%s';", newOverdraftBalanceInPennies, customerID);
    jdbcTemplate.update(overdraftBalanceUpdateSql);
  }

  public static void increaseCustomerOverdraftBalance(JdbcTemplate jdbcTemplate, String customerID, int increaseAmtInPennies) {
    String overdraftBalanceIncreaseSql = String.format("UPDATE Customers SET OverdraftBalance = OverdraftBalance + %d WHERE CustomerID='%s';", increaseAmtInPennies, customerID);
    jdbcTemplate.update(overdraftBalanceIncreaseSql);
  }

  public static void setCustomerCashBalance(JdbcTemplate jdbcTemplate, String customerID, int newBalanceInPennies) {
    String updateBalanceSql = String.format("UPDATE Customers SET Balance = %d WHERE CustomerID='%s';", newBalanceInPennies, customerID);
    jdbcTemplate.update(updateBalanceSql);
  }

  public static void increaseCustomerCashBalance(JdbcTemplate jdbcTemplate, String customerID, int increaseAmtInPennies) {
    String balanceIncreaseSql = String.format("UPDATE Customers SET Balance = Balance + %d WHERE CustomerID='%s';", increaseAmtInPennies, customerID);
    jdbcTemplate.update(balanceIncreaseSql);
  }

  public static void initCustomerCryptoBalance(JdbcTemplate jdbcTemplate, String customerID, String cryptoName) {
    // TODO: this currently does not check if row with customerID and cryptoName already exists, and can create a duplicate row!
    String balanceInitSql = "INSERT INTO CryptoHoldings (CryptoAmount,CustomerID,CryptoName) VALUES (0, ? , ? )";
    jdbcTemplate.update(balanceInitSql, customerID, cryptoName);
  }

  public static void increaseCustomerCryptoBalance(JdbcTemplate jdbcTemplate, String customerID, String cryptoName, double increaseAmt) {
    String balanceIncreaseSql = "UPDATE CryptoHoldings SET CryptoAmount = CryptoAmount + ? WHERE CustomerID= ? AND CryptoName= ?";
    jdbcTemplate.update(balanceIncreaseSql, increaseAmt, customerID, cryptoName);
  }

  public static void decreaseCustomerCashBalance(JdbcTemplate jdbcTemplate, String customerID, int decreaseAmtInPennies) {
    String balanceDecreaseSql = String.format("UPDATE Customers SET Balance = Balance - %d WHERE CustomerID='%s';", decreaseAmtInPennies, customerID);
    jdbcTemplate.update(balanceDecreaseSql);
  }

  public static void decreaseCustomerCryptoBalance(JdbcTemplate jdbcTemplate, String customerID, String cryptoName, double decreaseAmt) {
    String balanceDecreaseSql = "UPDATE CryptoHoldings SET CryptoAmount = CryptoAmount - ? WHERE CustomerID= ? AND CryptoName= ?";
    jdbcTemplate.update(balanceDecreaseSql, decreaseAmt, customerID, cryptoName);
  }

  public static void deleteRowFromOverdraftLogsTable(JdbcTemplate jdbcTemplate, String customerID, String timestamp) {
    String deleteRowFromOverdraftLogsSql = String.format("DELETE from OverdraftLogs where CustomerID='%s' AND Timestamp='%s';", customerID, timestamp);
    jdbcTemplate.update(deleteRowFromOverdraftLogsSql);
  }

  public static void insertRowToTransferLogsTable(JdbcTemplate jdbcTemplate, String customerID, String recipientID, String timestamp, int transferAmount) {
    String transferHistoryToSql = String.format("INSERT INTO TransferHistory VALUES ('%s', '%s', '%s', %d);",
                                                    customerID,
                                                    recipientID,
                                                    timestamp,
                                                    transferAmount);
    jdbcTemplate.update(transferHistoryToSql);
  }

  public static void insertRowToCryptoLogsTable(JdbcTemplate jdbcTemplate, String customerID, String cryptoName, String action, String timestamp, double cryptoAmount) {
    String cryptoHistorySql = "INSERT INTO CryptoHistory (CustomerID, Timestamp, Action, CryptoName, CryptoAmount) VALUES (?, ?, ?, ?, ?)";
    jdbcTemplate.update(cryptoHistorySql, customerID, timestamp, action, cryptoName, cryptoAmount);
  }
  
  public static boolean doesCustomerExist(JdbcTemplate jdbcTemplate, String customerID) { 
    String getCustomerIDSql =  String.format("SELECT CustomerID FROM Customers WHERE CustomerID='%s';", customerID);
    if (jdbcTemplate.queryForObject(getCustomerIDSql, String.class) != null) {
     return true;
    } else {
      return false;
    }
  }

  /* Final Project Starts */

  public static void insertRowToBorrowerRequests(JdbcTemplate jdbcTemplate, String customerID, String timestamp, String reasonForRequest, int moneyRequested) {
    String insertBorrowerSql = String.format("INSERT INTO BorrowerRequests VALUES ('%s', '%s', '%s', %d, %d);",
                                                    customerID,
                                                    timestamp,
                                                    reasonForRequest,
                                                    moneyRequested,
                                                    0);
    jdbcTemplate.update(insertBorrowerSql);
  }

  public static void setCustomerLiabilities(JdbcTemplate jdbcTemplate, String customerID, int liabilities) {
    String liabilitiesUpdateSql = String.format("UPDATE Customers SET Liabilities = %d WHERE CustomerID='%s';", liabilities, customerID);
    jdbcTemplate.update(liabilitiesUpdateSql);
  }

  public static int getCustomerLiabilities(JdbcTemplate jdbcTemplate, String customerID) {
    String getCustomerLiabilitiesSql = String.format("SELECT Liabilities FROM Customers WHERE CustomerID='%s';", customerID);
    int customerLiabilities = jdbcTemplate.queryForObject(getCustomerLiabilitiesSql, Integer.class);
    return customerLiabilities;
  }

  public static boolean doesBorrowRequestExist(JdbcTemplate jdbcTemplate, String borrowerID, String timestamp) {
    String getBorrowRequestSql =  String.format("SELECT CustomerId FROM BorrowerRequests WHERE CustomerID='%s' and Timestamp = '%s';", borrowerID, timestamp);
    if (jdbcTemplate.queryForObject(getBorrowRequestSql, String.class) != null) {
      return true;
     } else {
       return false;
     }
  }

  public static int getBorrowerRequestMoneyRequested(JdbcTemplate jdbcTemplate, String borrowerID, String timestamp) {
    String getBorrowRequestMoneyRequestedSql =  String.format("SELECT MoneyRequested FROM BorrowerRequests WHERE CustomerID='%s' and Timestamp = '%s';", borrowerID, timestamp);
    int moneyRequested = jdbcTemplate.queryForObject(getBorrowRequestMoneyRequestedSql, Integer.class);
    return moneyRequested;
  }

  public static int getBorrowerRequestMoneyRecieved(JdbcTemplate jdbcTemplate, String borrowerID, String timestamp) {
    String getBorrowRequestMoneyRecievedSql =  String.format("SELECT MoneyRecieved FROM BorrowerRequests WHERE CustomerID='%s' and Timestamp = '%s';", borrowerID, timestamp);
    int moneyRecieved= jdbcTemplate.queryForObject(getBorrowRequestMoneyRecievedSql, Integer.class);
    return moneyRecieved;
  }

  public static void insertRowToLiabilitiesTable(JdbcTemplate jdbcTemplate, String lenderID, String borrowerID, String timestamp, int amountOwed) {
    String insertRowToLiabilitiesSql = String.format("INSERT INTO LiabilitiesTable VALUES ('%s', '%s', '%s', %d);",
                                                    lenderID,
                                                    borrowerID,
                                                    timestamp,
                                                    amountOwed);
    jdbcTemplate.update(insertRowToLiabilitiesSql);
  }

  public static boolean doesLiabilityExist(JdbcTemplate jdbcTemplate, String lenderID, String borrowerID, String timestamp) {
    String getLiabilitySql = String.format("SELECT AmountOwed FROM LiabilitiesTable WHERE lenderID='%s' and borrowerID = '%s' and Timestamp = '%s';", lenderID, borrowerID, timestamp);
    if (jdbcTemplate.queryForObject(getLiabilitySql, Integer.class) != null) {
       return true;
     } else {
       return false;
     }
  }

  public static int getAmountOwed(JdbcTemplate jdbcTemplate, String lenderID, String borrowerID, String timestamp) {
    String getAmountOwedSql = String.format("SELECT AmountOwed FROM LiabilitiesTable WHERE lenderID='%s' and borrowerID = '%s' and Timestamp = '%s';", lenderID, borrowerID, timestamp);
    int amountOwed = jdbcTemplate.queryForObject(getAmountOwedSql, Integer.class);
    return amountOwed;
  }

  public static void deleteRowFromLiabilitiesTable(JdbcTemplate jdbcTemplate, String lenderID, String borrowerID, String timestamp) {
    String deleteRowFromiabilitiesTableSql = String.format("DELETE from LiabilitiesTable where lenderID='%s' AND borrowerID = '%s' AND Timestamp='%s';", lenderID, borrowerID, timestamp);
    jdbcTemplate.update(deleteRowFromiabilitiesTableSql);
  }

  public static void deleteRowFromBorrowerRequest(JdbcTemplate jdbcTemplate, String customerID, String timestamp) {
    String deleteRowBorrowerRequestSql = String.format("DELETE from BorrowerRequest where CustomerID='%s'AND Timestamp='%s';", customerID, timestamp);
    jdbcTemplate.update(deleteRowBorrowerRequestSql);
  }

  public static void setAmountDueInLiabilitiesTable(JdbcTemplate jdbcTemplate, String lenderID, String borrowerID, String timestamp, int amountOwed) {
    String setAmountDueInLiabilitiesTableSql = String.format("UPDATE LiabilitiesTable SET AmountOwed = %d WHERE LenderID='%s' AND BorrowerID = '%s' AND Timestamp = '%s';", 
                                                            amountOwed, 
                                                            lenderID,
                                                            borrowerID,
                                                            timestamp);
    jdbcTemplate.update(setAmountDueInLiabilitiesTableSql);
  }

  public static void setMoneyRecievedInBorrowerRequests(JdbcTemplate jdbcTemplate, String borrowerID, String timestamp, int moneyRecieved) {
    String setsetMoneyRecievedInBorrowerRequestsSql = String.format("Update BorrowerRequests SET MoneyRecieved = %d WHERE BorrowerID = '%s' AND Timestamp = '%s';",
                                                                    moneyRecieved,
                                                                    borrowerID,
                                                                    timestamp);
    jdbcTemplate.update(setsetMoneyRecievedInBorrowerRequestsSql);
  }

  /* Final Project Ends */
}
