alter table serie add hasnifti integer(1) NOT NULL DEFAULT 0 after voxelheight;
update serie set hasnifti=1 where serie.id = any(select id_serie from niftiimage);