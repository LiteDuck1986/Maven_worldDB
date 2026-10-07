package db;

import java.sql.Connection;

public class TestConnection {

	public static void main(String[] args) {
		try(Connection con =
				DatabaseConnection.getConnection()){
			System.out.println(
					"Savienojums ar datu bāzi ir izdevies: " +
			(con != null && !con.isClosed()));
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
