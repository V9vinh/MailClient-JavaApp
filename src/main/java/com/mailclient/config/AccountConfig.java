package com.mailclient.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mailclient.model.ReceiveProtocol;
import com.mailclient.model.SecurityMode;
import com.mailclient.model.SmtpEngineType;

/**
 * Cấu hình tài khoản gửi (SMTP) và nhận thư (POP3 / IMAP).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountConfig {
    private String displayName = "";
    private String emailAddress = "";

    // Cấu hình SMTP gửi thư
    private String smtpHost = "";
    private int smtpPort = 587;
    private SecurityMode securityMode = SecurityMode.STARTTLS;
    private SmtpEngineType engineType = SmtpEngineType.SOCKET;

    // Cấu hình nhận thư (POP3 / IMAP)
    private ReceiveProtocol receiveProtocol = ReceiveProtocol.POP3_SOCKET;
    private String receiveHost = "";
    private int receivePort = 995;
    private SecurityMode receiveSecurityMode = SecurityMode.SSL_TLS;

    // Xác thực
    private boolean authRequired = true;
    private String username = "";

    // Mật khẩu được giữ trong bộ nhớ phiên làm việc, không lưu vào file JSON
    @JsonIgnore
    private String sessionPassword = "";

    public AccountConfig() {
    }

    public AccountConfig copy() {
        AccountConfig c = new AccountConfig();
        c.displayName = this.displayName;
        c.emailAddress = this.emailAddress;
        c.smtpHost = this.smtpHost;
        c.smtpPort = this.smtpPort;
        c.securityMode = this.securityMode;
        c.engineType = this.engineType;

        c.receiveProtocol = this.receiveProtocol;
        c.receiveHost = this.receiveHost;
        c.receivePort = this.receivePort;
        c.receiveSecurityMode = this.receiveSecurityMode;

        c.authRequired = this.authRequired;
        c.username = this.username;
        c.sessionPassword = this.sessionPassword;
        return c;
    }

    public boolean isValid() {
        if (smtpHost == null || smtpHost.trim().isEmpty()) return false;
        if (smtpPort <= 0 || smtpPort > 65535) return false;
        if (emailAddress == null || !emailAddress.contains("@")) return false;
        if (authRequired) {
            return username != null && !username.trim().isEmpty()
                    && sessionPassword != null && !sessionPassword.isEmpty();
        }
        return true;
    }

    public boolean isReceiveValid() {
        if (receiveHost == null || receiveHost.trim().isEmpty()) return false;
        if (receivePort <= 0 || receivePort > 65535) return false;
        if (authRequired) {
            return username != null && !username.trim().isEmpty()
                    && sessionPassword != null && !sessionPassword.isEmpty();
        }
        return true;
    }

    // Getters and Setters
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName != null ? displayName : "";
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress != null ? emailAddress.trim() : "";
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost != null ? smtpHost.trim() : "";
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public SecurityMode getSecurityMode() {
        return securityMode;
    }

    public void setSecurityMode(SecurityMode securityMode) {
        this.securityMode = securityMode != null ? securityMode : SecurityMode.STARTTLS;
    }

    public SmtpEngineType getEngineType() {
        return engineType;
    }

    public void setEngineType(SmtpEngineType engineType) {
        this.engineType = engineType != null ? engineType : SmtpEngineType.SOCKET;
    }

    public ReceiveProtocol getReceiveProtocol() {
        return receiveProtocol != null ? receiveProtocol : ReceiveProtocol.POP3_SOCKET;
    }

    public void setReceiveProtocol(ReceiveProtocol receiveProtocol) {
        this.receiveProtocol = receiveProtocol != null ? receiveProtocol : ReceiveProtocol.POP3_SOCKET;
    }

    public String getReceiveHost() {
        return receiveHost != null ? receiveHost : "";
    }

    public void setReceiveHost(String receiveHost) {
        this.receiveHost = receiveHost != null ? receiveHost.trim() : "";
    }

    public int getReceivePort() {
        return receivePort > 0 ? receivePort : 995;
    }

    public void setReceivePort(int receivePort) {
        this.receivePort = receivePort;
    }

    public SecurityMode getReceiveSecurityMode() {
        return receiveSecurityMode != null ? receiveSecurityMode : SecurityMode.SSL_TLS;
    }

    public void setReceiveSecurityMode(SecurityMode receiveSecurityMode) {
        this.receiveSecurityMode = receiveSecurityMode != null ? receiveSecurityMode : SecurityMode.SSL_TLS;
    }

    public boolean isAuthRequired() {
        return authRequired;
    }

    public void setAuthRequired(boolean authRequired) {
        this.authRequired = authRequired;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username.trim() : "";
    }

    public String getSessionPassword() {
        return sessionPassword;
    }

    public void setSessionPassword(String sessionPassword) {
        this.sessionPassword = sessionPassword != null ? sessionPassword : "";
    }
}
