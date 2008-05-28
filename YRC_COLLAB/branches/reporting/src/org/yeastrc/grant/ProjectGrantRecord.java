package org.yeastrc.grant;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.yeastrc.db.DBConnectionManager;

public class ProjectGrantRecord {

	private ProjectGrantRecord(){}
	
	public static void saveProjectGrants(int projectID, List<Integer> grantIDs) throws SQLException {
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			// first clear all the existing grants for the project
			String sql = "DELETE FROM projectGrant WHERE projectID=" + projectID+" order by id";
			conn = DBConnectionManager.getConnection("yrc");
			stmt = conn.createStatement();
			int deleted = stmt.executeUpdate(sql);
			if (deleted == 0) {
				System.out.println("All old project grants deleted");
			}

			stmt.close(); stmt = null;
			conn.close(); conn = null;
			
			// now save the given grants
			conn = DBConnectionManager.getConnection("yrc");
			stmt = conn.createStatement();
			for(Integer grantID: grantIDs) {
				if (grantID <= 0)	continue; // this grant is not valid
				sql = "INSERT into projectGrant values(0,"+projectID+","+grantID+")";
				stmt.executeUpdate(sql);
			}
			stmt.close(); stmt = null;
			conn.close(); conn = null;

		} finally {

			// Always make sure result sets and statements are closed,
			// and the connection is returned to the pool
			if (rs != null) {
				try { rs.close(); } catch (SQLException e) { ; }
				rs = null;
			}
			if (stmt != null) {
				try { stmt.close(); } catch (SQLException e) { ; }
				stmt = null;
			}
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}
		}
	}
}
