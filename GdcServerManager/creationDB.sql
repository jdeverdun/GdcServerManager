--drop database if exists gdcserver;
--create database gdcserver;
use gdcserver;

create table User
(id integer, nom varchar(20), prenom varchar(20), email varchar(60), login varchar(20), password varchar(20), constraint user_pk primary key (id));

create table Project
(id integer, nom varchar(70),constraint project_pk primary key (id));

create table User_Project
(id_user integer, id_project integer,
constraint id_user_fk foreign key (id_user) references User(id) on delete cascade,
constraint id_project_fk foreign key (id_project) references Project(id) on delete cascade,
constraint user_project_pk primary key (id_user, id_project));

create table Patient
(id integer, nom varchar(50),id_project integer,
constraint patient_pk primary key (id),
constraint projectpatient_fk foreign key (id_project) references Project(id) on delete cascade);

create table AcquisitionDate
(id integer, acqdate date,id_project integer,id_patient integer,
constraint acquisitiondate_pk primary key (id),
constraint patientacqdate_fk foreign key (id_patient) references Patient(id) on delete cascade);

create table Protocol
(id integer, nom varchar(70),id_project integer,id_patient integer,id_acqdate integer,
constraint protocol_pk primary key (id),
constraint acqdateprotocol_fk foreign key (id_acqdate) references AcquisitionDate(id) on delete cascade);

create table Serie
(id integer, nom varchar(70),hasnifti tinyint(1),id_project integer,id_patient integer,id_acqdate integer,id_protocol integer,
constraint serie_pk primary key (id),
constraint protocolserie_fk foreign key (id_protocol) references Protocol(id) on delete cascade);

create table DicomImage
(id integer, nom varchar(50),id_project integer,id_patient integer,id_acqdate integer,id_protocol integer,id_serie integer,
constraint dicomimage_pk primary key (id),
constraint seriedicom_fk foreign key (id_serie ) references Serie(id) on delete cascade);

create table NiftiImage
(id integer, nom varchar(50),id_project integer,id_patient integer,id_acqdate integer,id_protocol integer,id_serie integer,
constraint niftiimage_pk primary key (id),
constraint serienifti_fk foreign key (id_serie) references Serie(id) on delete cascade);

