package dao;

import settings.SQLSettings;
import model.User;

public class Scripts {

	// script pour les creations de tables dans la database d'un nouveau projet
	// et les acces (grant)
	public static String[] getCreateUserViews(User u){
		String[] script = new String[14];
		// peut etre utiliser any(1,6,8) (enfin les id des projets stockés dans user)
		script[0] = "create view Project_"+u.getId()+" as select * from Project where Project.id = any(select id_project from User_Project where id_user="+u.getId()+");";
		script[1] = "create view Patient_"+u.getId()+" as select * from Patient where Patient.id_project = any(select id_project from User_Project where id_user="+u.getId()+");";
		script[2] = "create view AcquisitionDate_"+u.getId()+" as select * from AcquisitionDate where AcquisitionDate.id_project = any(select id_project from User_Project where id_user="+u.getId()+");";
		script[3] = "create view Protocol_"+u.getId()+" as select * from Protocol where Protocol.id_project = any(select id_project from User_Project where id_user="+u.getId()+");";
		script[4] = "create view DicomImage_"+u.getId()+" as select * from DicomImage where DicomImage.id_project = any(select id_project from User_Project where id_user="+u.getId()+");";
		script[5] = "create view NiftiImage_"+u.getId()+" as select * from NiftiImage where NiftiImage.id_project = any(select id_project from User_Project where id_user="+u.getId()+");";
		script[6] = "create view User_"+u.getId()+" as select * from User where User.id ="+u.getId()+";";
		
		script[7] = "GRANT SELECT ON `"+SQLSettings.DATABASE_NAME+"`.`Project_"+u.getId()+"` TO '"+u.getLogin()+"'@'';";
		script[8] = "GRANT SELECT ON `"+SQLSettings.DATABASE_NAME+"`.`Patient_"+u.getId()+"` TO '"+u.getLogin()+"'@'';";
		script[9] = "GRANT SELECT ON `"+SQLSettings.DATABASE_NAME+"`.`AcquisitionDate_"+u.getId()+"` TO '"+u.getLogin()+"'@'';";
		script[10] = "GRANT SELECT ON `"+SQLSettings.DATABASE_NAME+"`.`Protocol_"+u.getId()+"` TO '"+u.getLogin()+"'@'';";
		script[11] = "GRANT SELECT ON `"+SQLSettings.DATABASE_NAME+"`.`DicomImage_"+u.getId()+"` TO '"+u.getLogin()+"'@'';";
		script[12] = "GRANT SELECT ON `"+SQLSettings.DATABASE_NAME+"`.`NiftiImage_"+u.getId()+"` TO '"+u.getLogin()+"'@'';";
		script[12] = "GRANT SELECT ON `"+SQLSettings.DATABASE_NAME+"`.`User_"+u.getId()+"` TO '"+u.getLogin()+"'@'';";
		script[12] = "GRANT UPDATE ON `"+SQLSettings.DATABASE_NAME+"`.`User_"+u.getId()+"` TO '"+u.getLogin()+"'@'';";
		script[13] = "flush privileges;";
		
		return script;
	}
	
	

}
