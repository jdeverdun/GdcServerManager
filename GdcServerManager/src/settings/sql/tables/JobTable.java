package settings.sql.tables;

public class JobTable {
	
	// Les noms des champs
		public final String TNAME = "job"; // nom de la table
		private String id;
		private String user_id;
		private String jobid;
		private String submitDate;
		private String os;
		private String description;
		
		// Constructeur
		public JobTable(){
			id = "id";
			user_id = "id_user";
			jobid = "jobid";
			submitDate = "submitDate";
			os = "os";
			description = "description";
		}

		public String getUserId() {
			return user_id;
		}


		public String getJobId() {
			return jobid;
		}


		public String getId() {
			return id;
		}

		public String getSubmitDate() {
			return submitDate;
		}

		public String getOs() {
			return os;
		}

		public String getDescription() {
			return description;
		}

}
