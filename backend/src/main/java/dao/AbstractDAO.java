package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AbstractDAO {

    protected final Connection con;

    public AbstractDAO(final Connection con) {
        this.con = con;
    }

    protected void cleaningOperations(PreparedStatement stmnt, ResultSet result) throws SQLException {
        if (stmnt!=null) {
            stmnt.close();
        }
        if (result!=null) {
            result.close();
        }
    }

    protected void cleaningOperations(PreparedStatement stmnt) throws SQLException {
      cleaningOperations(stmnt, null);
    }


    public void closeConnection() throws SQLException {
        if (con!=null) {
            con.close();
        }
    }
}
