package service;

import java.util.prefs.Preferences;

public final class UserSession {

    private static volatile UserSession instance;

    private String userName;

    private String password;
    private String privileges;

    private UserSession(String userName, String password, String privileges) {
        this.userName = userName;
        this.password = password;
        this.privileges = privileges;
        Preferences userPreferences = Preferences.userRoot();
        userPreferences.put("USERNAME",userName);
        userPreferences.put("PASSWORD",password);
        userPreferences.put("PRIVILEGES",privileges);
    }



    public static UserSession getInstance(String userName,String password, String privileges) {
        if(instance == null) {
            synchronized (UserSession.class) {
                if(instance == null) {
                    instance = new UserSession(userName, password, privileges);
                }
            }
        } else {
            instance.setSessionData(userName, password, privileges);
        }
        return instance;
    }

    public static UserSession getInstance(String userName,String password) {
        return getInstance(userName,password,"NONE");
    }

    public static UserSession getCurrentSession() {
        return instance;
    }

    public synchronized String getUserName() {
        return userName;
    }

    public synchronized String getPassword() {
        return password;
    }

    public synchronized String getPrivileges() {
        return this.privileges;
    }

    public synchronized void cleanUserSession() {
        this.userName = "";// or null
        this.password = "";
        this.privileges = "";// or null

        Preferences userPreferences = Preferences.userRoot();
        userPreferences.remove("USERNAME");
        userPreferences.remove("PASSWORD");
        userPreferences.remove("PRIVILEGES");

        instance = null;
    }

    private synchronized void saveToPreferences() {
        Preferences userPreferences = Preferences.userRoot();
        userPreferences.put("USERNAME", userName);
        userPreferences.put("PASSWORD", password);
        userPreferences.put("PRIVILEGES", privileges);
    }

    public synchronized void setSessionData(String userName, String password, String privileges) {
        this.userName = userName;
        this.password = password;
        this.privileges = privileges;
        saveToPreferences();
    }

    @Override
    public synchronized String toString() {
        return "UserSession{" +
                "userName='" + this.userName + '\'' +
                ", privileges=" + this.privileges +
                '}';
    }
}
