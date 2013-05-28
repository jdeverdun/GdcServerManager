drop database if exists gdcserver;
create database gdcserver;
use gdcserver;

create table User
(id integer NOT NULL AUTO_INCREMENT, nom varchar(70), prenom varchar(70), email varchar(100), login varchar(50) UNIQUE, password varchar(100),level tinyint, firstconnect tinyint(1),
 constraint user_pk primary key (id));
 
create table User_View
(id integer NOT NULL AUTO_INCREMENT, login_user varchar(100), view_num integer,
constraint user_view_pk primary key (id),
constraint login_user_fk foreign key (login_user) references User(login) on delete cascade,
constraint login_view_unique unique(login_user,view_num));

create table Project
(id integer NOT NULL AUTO_INCREMENT, name varchar(100) UNIQUE, rkey varchar(50), constraint project_pk primary key (id));

create table User_Project
(id_user integer, id_project integer,
constraint id_user_fk foreign key (id_user) references User(id) on delete cascade,
constraint id_project_fk foreign key (id_project) references Project(id) on delete cascade,
constraint user_project_pk primary key (id_user, id_project));

create table Patient
(id integer NOT NULL AUTO_INCREMENT, name varchar(100), birthdate date, sex varchar(3), size float, weight float, id_project integer,
constraint patient_pk primary key (id),
constraint uprojet_namepat unique(name,id_project),
constraint projectpatient_fk foreign key (id_project) references Project(id) on delete cascade);

create table AcquisitionDate
(id integer NOT NULL AUTO_INCREMENT, date date,id_project integer,id_patient integer,
constraint acquisitiondate_pk primary key (id),
constraint upatient_projet_dateacqdate unique(date,id_project,id_patient),
constraint patientacqdate_fk foreign key (id_patient) references Patient(id) on delete cascade);

create table Protocol
(id integer NOT NULL AUTO_INCREMENT, name varchar(150),id_project integer,id_patient integer,id_acqdate integer,
constraint protocol_pk primary key (id),
constraint uacqdata_patient_projet_nameprot unique(name,id_project,id_patient,id_acqdate),
constraint acqdateprotocol_fk foreign key (id_acqdate) references AcquisitionDate(id) on delete cascade);

create table Serie
(id integer NOT NULL AUTO_INCREMENT, name varchar(150),mri_name varchar(50),repetitiontime float, echotime float, slicethickness float, voxelwidth float, voxelheight float, id_project integer,id_patient integer,id_acqdate integer,id_protocol integer,
constraint serie_pk primary key (id),
constraint uprot_acqdata_patient_projet_nameserie unique(name,id_project,id_patient,id_acqdate,id_protocol),
constraint protocolserie_fk foreign key (id_protocol) references Protocol(id) on delete cascade);

create table DicomImage
(id integer NOT NULL AUTO_INCREMENT, name varchar(300), slicelocation float,id_project integer,id_patient integer,id_acqdate integer,id_protocol integer,id_serie integer,
constraint dicomimage_pk primary key (id),
constraint uprot_serie_acqdata_patient_projet_namedicom unique(name,id_project,id_patient,id_acqdate,id_protocol,id_serie),
constraint seriedicom_fk foreign key (id_serie ) references Serie(id) on delete cascade);

create table NiftiImage
(id integer NOT NULL AUTO_INCREMENT, name varchar(300),slices integer, id_project integer,id_patient integer,id_acqdate integer,id_protocol integer,id_serie integer,
constraint niftiimage_pk primary key (id),
constraint uprot_serie_acqdata_patient_projet_namenifti unique(name,id_project,id_patient,id_acqdate,id_protocol,id_serie),
constraint serienifti_fk foreign key (id_serie) references Serie(id) on delete cascade);


-- INSERTION DE QUELQUES TUPLES
INSERT INTO `user` (`id`, `nom`, `prenom`, `email`, `login`, `password`, `level`, `firstconnect`) VALUES
(1, 'deverdun', 'jeremy', 'jeremy.deverdun@neuf.fr', 'jdeverdun', '*80182EDE2802545F7CB3F774677004CC510E85F6', 3, 0);

INSERT INTO `user_view` (`id`, `login_user`, `view_num`) VALUES
(1, 'jdeverdun', -10);


