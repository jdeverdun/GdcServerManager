package settings.sql.tables;

public class JobTable {
	
	// Les noms des champs
		public final String TNAME = "serie"; // nom de la table
		private String id;
		private String id_user;
		private String jobid;
		private String submitDate;
		private String os;
		private String description;
		
		// Constructeur
		public JobTable(){
			id = "id";
			id_user = "id_user";
			jobid = "jobid";
			submitDate = "submitDate";
			os = "os";
			description = "description";
		}

		public String getIdUser() {
			return id_user;
		}


		public String getJobid() {
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
