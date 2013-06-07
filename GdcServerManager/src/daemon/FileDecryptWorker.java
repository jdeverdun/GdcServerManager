package daemon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.logging.Level;

import settings.SystemSettings;
import settings.WindowManager;

import es.vocali.util.AESCrypt;

/**
 * Worker qui decrypt les fichiers
 * recupere toutes les infos grace aux chemins d'acces
 * @author Mobilette
 *
 */
public class FileDecryptWorker extends DaemonWorker {
	private Path from;
	private Path to;
	private DecryptDaemon decryptDaemon;
	
	public FileDecryptWorker(DecryptDaemon daemon, Path from, Path to){
		setDecryptDaemon(daemon);
		setFrom(from);
		setTo(to);
		setSerieFolder(from.getParent());
		Path protocolFolder = getSerieFolder().getParent();
		Path acqdateFolder = protocolFolder.getParent();
		setPatientFolder(acqdateFolder.getParent());
		setProjectFolder(getPatientFolder().getParent());
		setServerInfo(SystemSettings.SERVER_INFO);
	}
	
	
	
	// ------------------ Accesseurs --------------
	public Path getFrom() {
		return from;
	}
	public void setFrom(Path from) {
		this.from = from;
	}
	public Path getTo() {
		return to;
	}
	public void setTo(Path to) {
		this.to = to;
	}
	
	public DecryptDaemon getDecryptDaemon() {
		return decryptDaemon;
	}



	public void setDecryptDaemon(DecryptDaemon decryptDaemon) {
		this.decryptDaemon = decryptDaemon;
	}



	// ------------------- Methodes ---------------
	@Override
	public void start() {
		// AES crypt
		// Decrypte le fichier en enlevant l'extension definit dans AESCrypt.ENCRYPTSUFFIX
		try {
			AESCrypt aes = new AESCrypt(false, getAESPass());
			String filename = from.getFileName().toString().substring(0, from.getFileName().toString().length()-4);
			File nfile = new File(to+File.separator+filename+".part");// on met le .part tant qu'on copie
			aes.decrypt(from.toString(), nfile.getAbsolutePath());			
			// on renomme correctement
			nfile.renameTo(new File(nfile.getAbsolutePath().substring(0, nfile.getAbsolutePath().length()-5)));
		} catch (GeneralSecurityException | IOException e) {
			// Si le cryptage ne reussi pas je deplace vers un repertoire specifique
			WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningdicomdispatcher().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)));
			WindowManager.mwLogger.log(Level.SEVERE, "Decrypt error",e);
		}
		
		
		prepareToStop();
	}
	
	private void prepareToStop() {
		// rien a faire pour le moment
	}



	@Override
	protected void addEntryToDB(Path name, String table) {
		
	}
	
	
	
}
