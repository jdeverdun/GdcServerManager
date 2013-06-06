package dao;

import settings.SQLSettings;
import settings.sql.DBTables;
import model.User;

public class Scripts {

	// script pour les creations de tables dans la database d'un nouveau projet
	// et les acces (grant)
	public static String[] getCreateUserViews(User u){
		String[] script = new String[21];
		DBTables t = SQLSettings.TABLES;
		// peut etre utiliser any(1,6,8) (enfin les id des projets stockés dans user)
		script[0] = "create view "+t.getProject().TNAME+"_"+u.getId()+" as select * from "+t.getProject().TNAME+" where "+t.getProject().TNAME+"."+t.getProject().getId()+" = any(select "+t.getUser_project().getId_project()+" from "+t.getUser_project().TNAME+" where "+t.getUser_project().getId_user()+"="+u.getId()+");";
		script[1] = "create view "+t.getPatient().TNAME+"_"+u.getId()+" as select * from "+t.getPatient().TNAME+" where "+t.getPatient().TNAME+"."+t.getPatient().getId_project()+" = any(select "+t.getUser_project().getId_project()+" from "+t.getUser_project().TNAME+" where "+t.getUser_project().getId_user()+"="+u.getId()+");";
		script[2] = "create view "+t.getAcquisitionDate().TNAME+"_"+u.getId()+" as select * from "+t.getAcquisitionDate().TNAME+" where "+t.getAcquisitionDate().TNAME+"."+t.getAcquisitionDate().getId_project()+" = any(select "+t.getUser_project().getId_project()+" from "+t.getUser_project().TNAME+" where "+t.getUser_project().getId_user()+"="+u.getId()+");";
		script[3] = "create view "+t.getProtocol().TNAME+"_"+u.getId()+" as select * from "+t.getProtocol().TNAME+" where "+t.getProtocol().TNAME+"."+t.getProtocol().getId_project()+" = any(select "+t.getUser_project().getId_project()+" from "+t.getUser_project().TNAME+" where "+t.getUser_project().getId_user()+"="+u.getId()+");";
		script[4] = "create view "+t.getSerie().TNAME+"_"+u.getId()+" as select * from "+t.getSerie().TNAME+" where "+t.getSerie().TNAME+"."+t.getSerie().getId_project()+" = any(select "+t.getUser_project().getId_project()+" from "+t.getUser_project().TNAME+" where "+t.getUser_project().getId_user()+"="+u.getId()+");";
		script[5] = "create view "+t.getDicomImage().TNAME+"_"+u.getId()+" as select * from "+t.getDicomImage().TNAME+" where "+t.getDicomImage().TNAME+"."+t.getDicomImage().getId_project()+" = any(select "+t.getUser_project().getId_project()+" from "+t.getUser_project().TNAME+" where "+t.getUser_project().getId_user()+"="+u.getId()+");";
		script[6] = "create view "+t.getNiftiImage().TNAME+"_"+u.getId()+" as select * from "+t.getNiftiImage().TNAME+" where "+t.getNiftiImage().TNAME+"."+t.getNiftiImage().getId_project()+" = any(select "+t.getUser_project().getId_project()+" from "+t.getUser_project().TNAME+" where "+t.getUser_project().getId_user()+"="+u.getId()+");";
		script[7] = "create view "+t.getUser().TNAME+"_"+u.getId()+" as select * from "+t.getUser().TNAME+" where "+t.getUser().TNAME+".id ="+u.getId()+";";
		script[8] = "create view "+t.getUser_project().TNAME+"_"+u.getId()+" as select * from "+t.getUser_project().TNAME+" where "+t.getUser_project().TNAME+"."+t.getUser_project().getId_user()+" ='"+u.getId()+"';";
		
		script[9] = "GRANT SELECT ON `"+SQLSettings.DATABASE_NAME+"`.`"+t.getUser_view().TNAME+"` TO '"+u.getLogin()+"'@'';";
		script[10] = "GRANT SELECT ON `"+SQLSettings.DATABASE_NAME+"`.`"+t.getUser_project().TNAME+"_"+u.getId()+"` TO '"+u.getLogin()+"'@'';";
		script[11] = "GRANT SELECT ON `"+SQLSettings.DATABASE_NAME+"`.`"+t.getProject().TNAME+"_"+u.getId()+"` TO '"+u.getLogin()+"'@'';";
		script[12] = "GRANT SELECT ON `"+SQLSettings.DATABASE_NAME+"`.`"+t.getPatient().TNAME+"_"+u.getId()+"` TO '"+u.getLogin()+"'@'';";
		script[13] = "GRANT SELECT ON `"+SQLSettings.DATABASE_NAME+"`.`"+t.getAcquisitionDate().TNAME+"_"+u.getId()+"` TO '"+u.getLogin()+"'@'';";
		script[14] = "GRANT SELECT ON `"+SQLSettings.DATABASE_NAME+"`.`"+t.getProtocol().TNAME+"_"+u.getId()+"` TO '"+u.getLogin()+"'@'';";
		script[15] = "GRANT SELECT ON `"+SQLSettings.DATABASE_NAME+"`.`"+t.getSerie().TNAME+"_"+u.getId()+"` TO '"+u.getLogin()+"'@'';";
		script[16] = "GRANT SELECT ON `"+SQLSettings.DATABASE_NAME+"`.`"+t.getDicomImage().TNAME+"_"+u.getId()+"` TO '"+u.getLogin()+"'@'';";
		script[17] = "GRANT SELECT ON `"+SQLSettings.DATABASE_NAME+"`.`"+t.getNiftiImage().TNAME+"_"+u.getId()+"` TO '"+u.getLogin()+"'@'';";
		script[18] = "GRANT SELECT ON `"+SQLSettings.DATABASE_NAME+"`.`"+t.getUser().TNAME+"_"+u.getId()+"` TO '"+u.getLogin()+"'@'';";
		script[19] = "GRANT UPDATE ON `"+SQLSettings.DATABASE_NAME+"`.`"+t.getUser().TNAME+"_"+u.getId()+"` TO '"+u.getLogin()+"'@'';";
		script[20] = "flush privileges;";
		
		return script;
	}
	
	/**
	 * Script pour supprimer les vues d'un utilisateur
	 * @param u
	 * @return
	 */
	public static String[] getDeleteUserViews(User u){
		String[] script = new String[11];
		DBTables t = SQLSettings.TABLES;
		script[0] = "drop view "+t.getProject().TNAME+"_"+u.getId()+" ;";
		script[1] = "drop view "+t.getPatient().TNAME+"_"+u.getId()+" ;";
		script[2] = "drop view "+t.getAcquisitionDate().TNAME+"_"+u.getId()+" ;";
		script[3] = "drop view "+t.getProtocol().TNAME+"_"+u.getId()+" ;";
		script[4] = "drop view "+t.getDicomImage().TNAME+"_"+u.getId()+" ;";
		script[5] = "drop view "+t.getNiftiImage().TNAME+"_"+u.getId()+" ;";
		script[6] = "drop view "+t.getUser().TNAME+"_"+u.getId()+" ;";
		script[7] = "drop view "+t.getSerie().TNAME+"_"+u.getId()+" ;";
		script[8] = "drop view "+t.getUser_project().TNAME+"_"+u.getId()+" ;";
		script[9] = "REVOKE ALL PRIVILEGES, GRANT OPTION FROM "+u.getLogin()+";";
		script[10] = "flush privileges;";
		
		return script;
	}
	
	

}
