package dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public interface GenericRequestDAO {

	public HashMap<String,ArrayList<String>> execute(String request) throws SQLException;
}
