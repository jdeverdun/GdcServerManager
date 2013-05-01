package dao;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import daemon.DBCache;
import display.containers.RequestPanel.IMAGE_TYPE;

import exceptions.IllegalSQLRequest;

public interface GenericRequestDAO {

	public HashMap<String, ArrayList<String[]>> executeSelect(String request) throws SQLException, IllegalSQLRequest;
	public File buildPathFromIdList(String[] customFields, int type) throws SQLException;
	public HashMap<String, ArrayList<String[]>> executeFromRequestPanel(String project, String patient,
			String protocol, String serie, String begin, String end,
			IMAGE_TYPE imagetype) throws SQLException;
}
