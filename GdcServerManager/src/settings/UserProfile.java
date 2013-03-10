package settings;

import java.util.HashSet;
import java.util.Set;

import model.Project;
import model.User;

public class UserProfile {
	public static String LOGIN = null;
	public static String PASSWORD = null;
	public static String ENCRYPTEDPASS = null;
	public static User CURRENT_USER = null;
	public static Set<Project> PROJECTS = new HashSet<Project>();
	
	// Preferences liees au programme
	public static boolean SHOW_HIDDEN_FILES = false; 
	

	
	public static void reset(){
		LOGIN = null;
		PASSWORD = null;
		ENCRYPTEDPASS = null;
		PROJECTS = null;
		PROJECTS = new HashSet<Project>();
		CURRENT_USER = null;
	}
}
