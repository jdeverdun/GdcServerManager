package settings.sql.tables;

/**
 * Nom des champs de la table  User_View
 * sous la forme de fonctions
 * pour detecter plus facilement les zones
 * potentielles d'erreurs
 * @author DEVERDUN Jeremy
 *
 */
public class UserViewTable {
	
	// Les noms des champs
	public final String TNAME = "user_view"; // nom de la table
	private String id;
	private String login_user;
	private String view_num;
	
	// Constructeur
	public UserViewTable(){
		id = "id";
		login_user = "login_user";
		view_num = "view_num";
	}

	public String getLogin_user() {
		return login_user;
	}

	public String getView_num() {
		return view_num;
	}

	public String getId() {
		return id;
	}
}
