package dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import exceptions.IllegalSQLRequest;

public interface GenericRequestDAO {

	public HashMap<String,ArrayList<String>> executeSelect(String request) throws SQLException, IllegalSQLRequest;
}
