package com.mailclient.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

/**
 * Quản lý nạp và lưu trữ cấu hình ứng dụng vào tệp JSON cục bộ.
 */
public class ConfigManager {
    private static final String CONFIG_FILE_NAME = "mail_config.json";
    private final File configFile;
    private final ObjectMapper mapper;
    private AccountConfig currentAccount;

    public ConfigManager() {
        this.configFile = new File(CONFIG_FILE_NAME);
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        loadConfig();
    }

    public synchronized AccountConfig getCurrentAccount() {
        if (currentAccount == null) {
            currentAccount = new AccountConfig();
        }
        return currentAccount;
    }

    public synchronized void updateAccount(AccountConfig newAccount) {
        if (newAccount != null) {
            this.currentAccount = newAccount;
            saveConfig();
        }
    }

    public synchronized void loadConfig() {
        if (configFile.exists() && configFile.isFile()) {
            try {
                this.currentAccount = mapper.readValue(configFile, AccountConfig.class);
            } catch (Exception e) {
                System.err.println("Không thể đọc tệp cấu hình: " + e.getMessage());
                this.currentAccount = new AccountConfig();
            }
        } else {
            this.currentAccount = new AccountConfig();
        }
    }

    public synchronized boolean saveConfig() {
        if (currentAccount == null) return false;
        try {
            mapper.writeValue(configFile, currentAccount);
            return true;
        } catch (IOException e) {
            System.err.println("Không thể ghi tệp cấu hình: " + e.getMessage());
            return false;
        }
    }
}
