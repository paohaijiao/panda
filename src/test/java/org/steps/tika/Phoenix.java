package org.steps.tika;

import java.sql.*;

/**
 * 一定要java8 不然
 */
public class Phoenix {
    private static String driver = "org.apache.phoenix.jdbc.PhoenixDriver";

    public static void main(String[] args) throws SQLException {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Statement stmt = null;
        ResultSet rs = null;

        Connection con = DriverManager.getConnection("jdbc:phoenix:cdh:2181");
        stmt = con.createStatement();
        String sql = " select * from USERS";
        rs = stmt.executeQuery(sql);
        while (rs.next()) {
            System.out.print("USERNAME:"+rs.getString("USERNAME"));
        }
        stmt.close();
        con.close();
    }
}
