package settings;

import java.util.HashSet;
import java.util.Set;

import model.Project;
import model.User;

public class UserProfile {
	public static String LOGIN = null;
	public static String PASSWORD = null;
	public static String ENCRYPTEDPASS = null;
	public static User CURRENT_USER;
	public static Set<Project> PROJECTS = new HashSet<Project>();
	
	public static void reset(){
		LOGIN = null;
		PASSWORD = null;
		ENCRYPTEDPASS = null;
		PROJECTS = null;
		PROJECTS = new HashSet<Project>();
		CURRENT_USER = null;
	}
}
