package dao;

import model.User;

public class Scripts {

	// script pour les creations de tables dans la database d'un nouveau projet
	public static String[] createUserViews(User u){
		String[] script = new String[6];
		// peut etre utiliser any(1,6,8) (enfin les id des projets stockés dans user)
		script[0] = "create view Project_"+u.getId()+" as select * from Project where Project.id = any(select id_project from User_Project where id_user="+u.getId()+");";
		script[0] = "create view Project_"+u.getId()+" as select * from Project where Project.id = any(select id_project from User_Project where id_user="+u.getId()+");";
	}
}
