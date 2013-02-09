--drop database if exists gdcserver;
--create database gdcserver;
use gdcserver;

create table User
(id integer, nom varchar(20), prenom varchar(20), email varchar(60), login varchar(20), password varchar(20), constraint user_pk primary key (id));

create table Project
(id integer, nom varchar(50),constraint project_pk primary key (id));

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
(id integer, date varchar(50),id_patient integer,
constraint acquisitiondate_pk primary key (id),
constraint patientacqdate_fk foreign key (id_patient) references Patient(id) on delete cascade);

create table Protocol
(id integer, nom varchar(50),id_acqdate integer,
constraint protocol_pk primary key (id),
constraint acqdateprotocol_fk foreign key (id_acqdate) references AcquisitionDate(id) on delete cascade);

create table Serie
(id integer, nom varchar(50),id_protocol integer,
constraint serie_pk primary key (id),
constraint protocolserie_fk foreign key (id_protocol) references Protocol(id) on delete cascade);



create table Falaise
(id integer, nom varchar(50),
ville_id integer, pays_id integer,
constraint pays_fk foreign key (pays_id) references Pays(id) on delete cascade,
constraint ville_id_fk foreign key (ville_id) references Ville(id) on delete cascade,
constraint falaise_pk primary key (id));



create table Grimpeur_Falaise
(id_grimpeur integer, id_falaise integer,
constraint id_grimpeur_fk foreign key (id_grimpeur) references Grimpeur(id) on delete cascade,
constraint id_falaise_fk foreign key (id_falaise) references Falaise(id) on delete cascade,
constraint grimpeur_falaise_pk primary key (id_grimpeur, id_falaise));

create table Voie
(id integer, nom varchar(30), degre integer, lettre varchar(1), nuance varchar(1), falaise_id integer,
constraint falaise_fk foreign key (falaise_id) references Falaise(id) on delete cascade,
 constraint voie_pk primary key (id));
 
 create table Grimpeur_Voie
(id_grimpeur integer, id_voie integer,
constraint grimpeur_voie_pk primary key (id_grimpeur, id_voie),
constraint id_grimpeur_fkGV foreign key (id_grimpeur) references Grimpeur(id) on delete cascade,
constraint id_voie_fkGV foreign key (id_voie) references Voie(id) on delete cascade
);

create table Ascension
(id integer, dateA varchar(70),
comment varchar(100), etoile integer, note integer, recommande boolean, ressentiDifficulte integer, style integer,
id_grimpeur integer, id_voie integer,
constraint id_grim_fk_A foreign key (id_grimpeur) references Grimpeur(id) on delete cascade,
constraint id_voie_fk_A foreign key (id_voie) references Voie(id) on delete cascade,
constraint ascension_pk primary key (id));

create table GrimpeurEnAttente (
	nom varchar(15), 
	prenom varchar(15), 
	email varchar(40), 
	poids integer, 
	age integer, 
	login varchar(15), 
	password varchar(15), 
	clef varchar(20),
	constraint user_pk primary key (clef));



insert into Grimpeur values (1,'Martin','Pierre','pierre.martin@univ-montp2.fr', 70, 25, 'pierre', 'martin');
insert into Grimpeur values (2,'Dupond','Emilie','emilie.dupond@univ-montp2.fr', 65, 23, 'emilie', 'dupond');
insert into Grimpeur values (3,'Cazenave','Daniel','daniel.cazenave@univ-montp2.fr', 80, 27, 'daniel','cazenave');
insert into Grimpeur values (4,'Cazenave','Elodie','elodie.cazenave@univ-montp2.fr', 55, 21, 'elodie', 'cazenave');


insert into Pays Values(1, 'France');
insert into Pays Values(2, 'Guatemala');

insert into Ville Values(1, 'Pinalota', 2);
insert into Ville Values(2, 'Saint-beauzille de Montmelle', 1);
insert into Ville Values(3, 'Claret', 1);

insert into Falaise Values(1, 'Saint-beauzille de Montmelle',2, 1);
insert into Falaise Values(2, 'Claret',3, 1);
insert into Falaise Values(3, 'Disgra',1, 2);

insert into Grimpeur_Falaise Values(1,1);
insert into Grimpeur_Falaise Values(1,2);
insert into Grimpeur_Falaise Values(2,1);
insert into Grimpeur_Falaise Values(2,2);
insert into Grimpeur_Falaise Values(3,1);
insert into Grimpeur_Falaise Values(4,2);

insert into Voie Values(1,'L ange exterminateur',6, 'c','',1);
insert into Voie Values(2,'Le sot met des idiots',6, 'c','+',1);
insert into Voie Values(3,'CLPA',5, 'c','+',1);
insert into Voie Values(4,'Octobre rouge',6, 'c','',1);
insert into Voie Values(5,'Clair Obscur',6, 'c','',2);
insert into Voie Values(6,'Le nain compris',4, 'b','',3);


insert into Grimpeur_Voie Values(1,1);
insert into Grimpeur_Voie Values(1,2);
insert into Grimpeur_Voie Values(1,3);
insert into Grimpeur_Voie Values(1,4);
insert into Grimpeur_Voie Values(1,5);
insert into Grimpeur_Voie Values(2,1);
insert into Grimpeur_Voie Values(2,5);
insert into Grimpeur_Voie Values(3,2);
insert into Grimpeur_Voie Values(3,3);
insert into Grimpeur_Voie Values(3,4);
insert into Grimpeur_Voie Values(3,1);
insert into Grimpeur_Voie Values(4,5);