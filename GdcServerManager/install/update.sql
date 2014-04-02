# update 1

alter table serie add hasnifti integer(1) NOT NULL DEFAULT 0 after voxelheight;
update serie set hasnifti=1 where serie.id = any(select id_serie from niftiimage);


# update 2 (20/06/2013)
alter table patient add rkey varchar(100) NOT NULL DEFAULT 'nokey' after weight;
update patient, project set patient.rkey=project.rkey where patient.id_project = project.id;
alter table project drop rkey;

# update 3 (01/04/2014)
ALTER TABLE serie ADD COLUMN impossibleNiftiConversion int(1) DEFAULT 0;
ALTER TABLE serie MODIFY COLUMN impossibleNiftiConversion int(1) DEFAULT 0 AFTER hasnifti;