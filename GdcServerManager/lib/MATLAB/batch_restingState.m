try
    mapdrive;
    folder = '#15#';
    t1reg = 'T1';
    restreg = 'REST';
    grereg = 'gre_field';
    cd (folder);
    clear pinfo
    pinfo.name = '#16#';
    pinfo.dir = folder;
    date=#18#;
    if(date==0)
        pinfo.acquisition='#19#';
    end
    t1dir = dir(['*' t1reg '*']);
    restdir = dir(['*' restreg '*']);
    gredir = dir(['*' grereg '*']);
    protocol=#20#;
    i=1;
    j=1;
    if(protocol==0)
        t1dir2 = dir([t1dir.name '/*' t1reg '*']);
        restdir2 = dir([restdir.name '/*' restreg '*']);
        gredir2 = dir([gredir.name '/*' grereg '*']);
        while(i<=numel(t1dir2))
            if(isempty(regexpi(t1dir2(i).name, '.*T1.*3D.*GADO.*')))
                i=i+1;
            else
                j=i;
                i=numel(t1dir2)+1;
            end
        end
        pinfo.t1.dir = [pinfo.dir '\' t1dir.name '\' t1dir2(j).name];
        pinfo.rest.dir = [pinfo.dir '\' restdir.name '\' restdir(1).name];
        pinfo.grefield.dir = [pinfo.dir '\' gredir.name '\' gredir(1).name];
    else
        pinfo.t1.dir = [pinfo.dir '\' t1dir(1).name];
        pinfo.rest.dir = [pinfo.dir '\' restdir(1).name];
        pinfo.grefield.dir = [pinfo.dir '\' gredir(1).name];
    end
    
    pinfo.t1.files = spm_select('FPList',pinfo.t1.dir,['^' pinfo.name '.*.nii']);
    pinfo.rest.files = spm_select('FPList',pinfo.rest.dir,['^' pinfo.name '.*.nii']);
	RmImage=#21#;
	if(RmImage==0)
		pinfo.rest.files=pinfo.rest.files(#22#+1:end,:);
	end
    if(~(isempty(gredir)))
        pinfo.grefield.files = spm_select('FPList',pinfo.grefield.dir,['^' pinfo.name '.*.nii']);
        if(isempty(pinfo.grefield.files))
            error(['Empty grefield files for ' pinfo.name]);
        end
        pinfo.all.files= char(pinfo.t1.files, pinfo.rest.files, pinfo.grefield.files);
    else
        pinfo.all.files= char(pinfo.t1.files, pinfo.rest.files);
    end
    if(isempty(pinfo.t1.files))
        error(['Empty t1 files for ' pinfo.name]);
    end
    if(isempty(pinfo.rest.files))
        error(['Empty rest files for ' pinfo.name]);
    end
    path2job=mfilename('fullpath');
	[path2job ~, ~] = fileparts(path2job);
    %% reorientation - optionnel
    reorientationFlag=#1#;
    if(reorientationFlag==0)
        clear matlabbatch
        matlabbatch{1}.spm.util.reorient.srcfiles = cellstr(pinfo.all.files);
		[~, xlsname, ext] = fileparts('#2#');
        [num txt raw]=xlsread([path2job filesep xlsname ext]);
        [ligne colonne]=size(raw);
		for i=1:ligne
			if(isequal(num2str(raw{i,1}),'NaN'))
				raw{i,1}=raw{i-1,1};
			end
		end
        trouve=false;
        i=1;
        if(date==0)
            while(i<=ligne)
                if((isequal(num2str(raw{i,1}),pinfo.name) || isequal(raw{i,1},pinfo.name)) && isequal(num2str(raw{i,2}),pinfo.acquisition))
                    trouve=true;
                    break;
                else
                    i=i+1;
                end
            end
            if(trouve)
                matlabbatch{1}.spm.util.reorient.transform.transprm = [-(str2num(raw{i,3})) -(str2num(raw{i,4})) -(str2num(raw{i,5})) 0 0 0 1 1 1 0 0 0];
                if(isequal(class(raw{i,1}),'double'))
                    fprintf(['ligne : ' num2str(i) ', name : ' num2str(raw{i,1}) ', date : ' num2str(raw{i,2}) ', x : ' num2str(raw{i,3}) ', y : ' num2str(raw{i,4}) ', z : ' num2str(raw{i,5})]);
                else
                    fprintf(['ligne : ' num2str(i) ', name : ' raw{i,1} ', date : ' num2str(raw{i,2}) ', x : ' num2str(raw{i,3}) ', y : ' num2str(raw{i,4}) ', z : ' num2str(raw{i,5})]);
                end
            else
                error(['Patient ' pinfo.name ' : not find in the xls file at the date ' pinfo.acquisition]);
            end
        else
            while(i<=ligne)
                if(isequal(num2str(raw{i,1}),pinfo.name) || isequal(raw{i,1},pinfo.name))
                    trouve=true;
                    break;
                else
                    i=i+1;
                end
            end
            if(trouve)
                matlabbatch{1}.spm.util.reorient.transform.transprm = [-(str2num(raw{i,2})) -(str2num(raw{i,3})) -(str2num(raw{i,4})) 0 0 0 1 1 1 0 0 0];
                if(isequal(class(raw{i,1}),'double'))
                    fprintf(['ligne : ' num2str(i) ', name : ' num2str(raw{i,1}) ', x : ' num2str(raw{i,2}) ', y : ' num2str(raw{i,3}) ', z : ' num2str(raw{i,4})]);
                else
                    fprintf(['ligne : ' num2str(i) ', name : ' raw{i,1} ', x : ' num2str(raw{i,2}) ', y : ' num2str(raw{i,3}) ', z : ' num2str(raw{i,4})]);
                end
            else
                error(['Patient ' pinfo.name ' : not find in the xls file']);
            end
        end
        matlabbatch{1}.spm.util.reorient.prefix = 'o_';
        spm_jobman('run',matlabbatch);
        pinfo.t1.files = spm_select('FPList',pinfo.t1.dir,['^o_' pinfo.name '.*.nii']);
        pinfo.rest.files = spm_select('FPList',pinfo.rest.dir,['^o_' pinfo.name '.*.nii']);
        pinfo.grefield.files = spm_select('FPList',pinfo.grefield.dir,['^o_' pinfo.name '.*.nii']);
    end
    
    if(reorientationFlag==2)
        pinfo.t1.files = spm_select('FPList',pinfo.t1.dir,['^o_' pinfo.name '.*.nii']);
        pinfo.rest.files = spm_select('FPList',pinfo.rest.dir,['^o_' pinfo.name '.*.nii']);
        pinfo.grefield.files = spm_select('FPList',pinfo.grefield.dir,['^o_' pinfo.name '.*.nii']);
    end
    
    %% presubstract phase and magnitude - optionnel
    presubtractFlag=#3#;
    if(presubtractFlag==0)
        defaultsfile=#17#;
        clear matlabbatch
        matlabbatch{1}.spm.tools.fieldmap.presubphasemag.subj.phase = cellstr(pinfo.grefield.files(3,:));
        matlabbatch{1}.spm.tools.fieldmap.presubphasemag.subj.magnitude = cellstr(pinfo.grefield.files(2,:));
        if(defaultsfile==0)
			[~, pmname, ext] = fileparts('#4#');
            matlabbatch{1}.spm.tools.fieldmap.presubphasemag.subj.defaults.defaultsfile = {[path2job filesep pmname ext]}; %utilisateur parametre depend de la machine
        else
            matlabbatch{1}.spm.tools.fieldmap.presubphasemag.subj.defaults.defaultsfile = {[spm('Dir') '\toolbox\FieldMap\pm_defaults_skyra.m']};
        end
        matlabbatch{1}.spm.tools.fieldmap.presubphasemag.subj.session.epi = cellstr(pinfo.rest.files(1,:));
        matlabbatch{1}.spm.tools.fieldmap.presubphasemag.subj.matchvdm = 1;
        matlabbatch{1}.spm.tools.fieldmap.presubphasemag.subj.sessname = 'session';
        matlabbatch{1}.spm.tools.fieldmap.presubphasemag.subj.writeunwarped = 1;
        matlabbatch{1}.spm.tools.fieldmap.presubphasemag.subj.anat = cellstr(pinfo.t1.files);
        matlabbatch{1}.spm.tools.fieldmap.presubphasemag.subj.matchanat = 0;
        spm_jobman('run',matlabbatch);
        if(reorientationFlag==0 || reorientationFlag==2)
            pinfo.grefield.vdm5.files = spm_select('FPList',pinfo.grefield.dir,['^vdm5_sco_.*' pinfo.name '.*.nii']);
        else
            pinfo.grefield.vdm5.files = spm_select('FPList',pinfo.grefield.dir,['^vdm5_sc.*' pinfo.name '.*.nii']);
        end
        if(isempty(pinfo.grefield.vdm5.files))
            error(['Empty grefield.vdm files for ' pinfo.name]);
        end
    end
    if(presubtractFlag==2)
        if(reorientationFlag==0 || reorientationFlag==2)
            pinfo.grefield.vdm5.files = spm_select('FPList',pinfo.grefield.dir,['^vdm5_sco_.*' pinfo.name '.*.nii']);
        else
            pinfo.grefield.vdm5.files = spm_select('FPList',pinfo.grefield.dir,['^vdm5_sc.*' pinfo.name '.*.nii']);
        end
        if(isempty(pinfo.grefield.vdm5.files))
            error(['Empty grefield.vdm files for ' pinfo.name]);
        end
    end
    %% slice timing resting state
    clear matlabbatch
    matlabbatch{1}.spm.temporal.st.scans = {cellstr(pinfo.rest.files)};
    matlabbatch{1}.spm.temporal.st.nslices = #5#;%utilisateur
    matlabbatch{1}.spm.temporal.st.tr = #6#;%utilisateur
    matlabbatch{1}.spm.temporal.st.ta = #7#;%utilisateur
    matlabbatch{1}.spm.temporal.st.so = [#8#];%utilisateur
    matlabbatch{1}.spm.temporal.st.refslice = #9#;%utilisateur
    matlabbatch{1}.spm.temporal.st.prefix = 'a';
    spm_jobman('run',matlabbatch);
    if(reorientationFlag==0 || reorientationFlag==2)
        pinfo.rest.st.files = spm_select('FPList',pinfo.rest.dir,['^ao_' pinfo.name '.*.nii']);
		
    else
        pinfo.rest.st.files = spm_select('FPList',pinfo.rest.dir,['^a' pinfo.name '.*.nii']);
    end
    if(isempty(pinfo.rest.st.files))
        error(['Empty rest.st.files files for ' pinfo.name]);
    end
    
    %% realignement du resting state avec grefield
    if(presubtractFlag==0 || presubtractFlag==2)
        clear matlabbatch
        matlabbatch{1}.spm.spatial.realignunwarp.data.scans = cellstr(pinfo.rest.st.files);
        matlabbatch{1}.spm.spatial.realignunwarp.data.pmscan = cellstr(pinfo.grefield.vdm5.files);
        matlabbatch{1}.spm.spatial.realignunwarp.eoptions.quality = 0.9;
        matlabbatch{1}.spm.spatial.realignunwarp.eoptions.sep = 4;
        matlabbatch{1}.spm.spatial.realignunwarp.eoptions.fwhm = 5;
        matlabbatch{1}.spm.spatial.realignunwarp.eoptions.rtm = 1;
        matlabbatch{1}.spm.spatial.realignunwarp.eoptions.einterp = 2;
        matlabbatch{1}.spm.spatial.realignunwarp.eoptions.ewrap = [0 0 0];
        matlabbatch{1}.spm.spatial.realignunwarp.eoptions.weight = '';
        matlabbatch{1}.spm.spatial.realignunwarp.uweoptions.basfcn = [12 12];
        matlabbatch{1}.spm.spatial.realignunwarp.uweoptions.regorder = 1;
        matlabbatch{1}.spm.spatial.realignunwarp.uweoptions.lambda = 100000;
        matlabbatch{1}.spm.spatial.realignunwarp.uweoptions.jm = 0;
        matlabbatch{1}.spm.spatial.realignunwarp.uweoptions.fot = [4 5];
        matlabbatch{1}.spm.spatial.realignunwarp.uweoptions.sot = [];
        matlabbatch{1}.spm.spatial.realignunwarp.uweoptions.uwfwhm = 4;
        matlabbatch{1}.spm.spatial.realignunwarp.uweoptions.rem = 1;
        matlabbatch{1}.spm.spatial.realignunwarp.uweoptions.noi = 5;
        matlabbatch{1}.spm.spatial.realignunwarp.uweoptions.expround = 'Average';
        matlabbatch{1}.spm.spatial.realignunwarp.uwroptions.uwwhich = [2 1];
        matlabbatch{1}.spm.spatial.realignunwarp.uwroptions.rinterp = 4;
        matlabbatch{1}.spm.spatial.realignunwarp.uwroptions.wrap = [0 0 0];
        matlabbatch{1}.spm.spatial.realignunwarp.uwroptions.mask = 0;
        matlabbatch{1}.spm.spatial.realignunwarp.uwroptions.prefix = 'u';
        spm_jobman('run',matlabbatch);
        
        if(reorientationFlag==0 || reorientationFlag==2)
            pinfo.rest.realign.files = spm_select('FPList',pinfo.rest.dir,['^uao_' pinfo.name '.*.nii']);
            pinfo.rest.realign.mean.files = spm_select('FPList',pinfo.rest.dir,['^meanuao_' pinfo.name '.*.nii']);
        else
            pinfo.rest.realign.files = spm_select('FPList',pinfo.rest.dir,['^ua' pinfo.name '.*.nii']);
            pinfo.rest.realign.mean.files = spm_select('FPList',pinfo.rest.dir,['^meanua' pinfo.name '.*.nii']);
        end
        
        if(isempty(pinfo.rest.realign.files))
            error(['Empty rest.realign.files files for ' pinfo.name]);
        end
        
        if(isempty(pinfo.rest.realign.mean.files))
            error(['Empty rest.realign.mean.files files for ' pinfo.name]);
        end
    else
        % realignement du resting state sans grefield
        matlabbatch{1}.spm.spatial.realign.estwrite.data = cellstr(pinfo.rest.st.files);
        matlabbatch{1}.spm.spatial.realign.estwrite.eoptions.quality = 0.9;
        matlabbatch{1}.spm.spatial.realign.estwrite.eoptions.sep = 4;
        matlabbatch{1}.spm.spatial.realign.estwrite.eoptions.fwhm = 5;
        matlabbatch{1}.spm.spatial.realign.estwrite.eoptions.rtm = 1;
        matlabbatch{1}.spm.spatial.realign.estwrite.eoptions.interp = 2;
        matlabbatch{1}.spm.spatial.realign.estwrite.eoptions.wrap = [0 0 0];
        matlabbatch{1}.spm.spatial.realign.estwrite.eoptions.weight = '';
        matlabbatch{1}.spm.spatial.realign.estwrite.roptions.which = [2 1];
        matlabbatch{1}.spm.spatial.realign.estwrite.roptions.interp = 4;
        matlabbatch{1}.spm.spatial.realign.estwrite.roptions.wrap = [0 0 0];
        matlabbatch{1}.spm.spatial.realign.estwrite.roptions.mask = 1;
        matlabbatch{1}.spm.spatial.realign.estwrite.roptions.prefix = 'r';
        spm_jobman('run',matlabbatch);
        if(reorientationFlag==0 || reorientationFlag==2)
            pinfo.rest.realign.files = spm_select('FPList',pinfo.rest.dir,['^rao_' pinfo.name '.*.nii']);
            pinfo.rest.realign.mean.files = spm_select('FPList',pinfo.rest.dir,['^meanrao_' pinfo.name '.*.nii']);
        else
            pinfo.rest.realign.files = spm_select('FPList',pinfo.rest.dir,['^ra' pinfo.name '.*.nii']);
            pinfo.rest.realign.mean.files = spm_select('FPList',pinfo.rest.dir,['^meanra' pinfo.name '.*.nii']);
        end
        
        if(isempty(pinfo.rest.realign.files))
            error(['Empty rest.realign.files files for ' pinfo.name]);
        end
        
        if(isempty(pinfo.rest.realign.mean.files))
            error(['Empty rest.realign.mean.files files for ' pinfo.name]);
        end
    end
    %% coregistration du resting state
    clear matlabbatch
    matlabbatch{1}.spm.spatial.coreg.estimate.ref = cellstr(pinfo.t1.files);
    matlabbatch{1}.spm.spatial.coreg.estimate.source = cellstr(pinfo.rest.realign.mean.files);
    matlabbatch{1}.spm.spatial.coreg.estimate.other = cellstr(pinfo.rest.realign.files);
    matlabbatch{1}.spm.spatial.coreg.estimate.eoptions.cost_fun = 'nmi';
    matlabbatch{1}.spm.spatial.coreg.estimate.eoptions.sep = [4 2];
    matlabbatch{1}.spm.spatial.coreg.estimate.eoptions.tol = [0.02 0.02 0.02 0.001 0.001 0.001 0.01 0.01 0.01 0.001 0.001 0.001];
    matlabbatch{1}.spm.spatial.coreg.estimate.eoptions.fwhm = [7 7];
    spm_jobman('run',matlabbatch);
    
    %% segmentation
    clear matlabbatch
    matlabbatch{1}.spm.spatial.preproc.data = cellstr(pinfo.t1.files);
    matlabbatch{1}.spm.spatial.preproc.output.GM = [0 0 1];
    matlabbatch{1}.spm.spatial.preproc.output.WM = [0 0 1];
    matlabbatch{1}.spm.spatial.preproc.output.CSF = [0 0 1];
    matlabbatch{1}.spm.spatial.preproc.output.biascor = 1;
    matlabbatch{1}.spm.spatial.preproc.output.cleanup = 0;
    matlabbatch{1}.spm.spatial.preproc.opts.tpm = {
        [spm('Dir') '\tpm\grey.nii']
        [spm('Dir') '\tpm\white.nii']
        [spm('Dir') '\tpm\csf.nii']
        };
    matlabbatch{1}.spm.spatial.preproc.opts.ngaus = [2
        2
        2
        4];
    matlabbatch{1}.spm.spatial.preproc.opts.regtype = 'mni';
    matlabbatch{1}.spm.spatial.preproc.opts.warpreg = 1;
    matlabbatch{1}.spm.spatial.preproc.opts.warpco = 25;
    matlabbatch{1}.spm.spatial.preproc.opts.biasreg = 0.0001;
    matlabbatch{1}.spm.spatial.preproc.opts.biasfwhm = 60;
    matlabbatch{1}.spm.spatial.preproc.opts.samp = 3;
    matlabbatch{1}.spm.spatial.preproc.opts.msk = {''};
    spm_jobman('run',matlabbatch);
	
	if(reorientationFlag==0 || reorientationFlag==2)
        pinfo.t1.segment.gm = spm_select('FPList',pinfo.t1.dir,['^c1o_' pinfo.name '.*.nii']);
		pinfo.t1.segment.wm = spm_select('FPList',pinfo.t1.dir,['^c2o_' pinfo.name '.*.nii']);
		pinfo.t1.segment.csf = spm_select('FPList',pinfo.t1.dir,['^c3o_' pinfo.name '.*.nii']);
    else
        pinfo.t1.segment.gm = spm_select('FPList',pinfo.t1.dir,['^c1' pinfo.name '.*.nii']);
		pinfo.t1.segment.wm = spm_select('FPList',pinfo.t1.dir,['^c2' pinfo.name '.*.nii']);
		pinfo.t1.segment.csf = spm_select('FPList',pinfo.t1.dir,['^c3' pinfo.name '.*.nii']);
    end
    %% Normalisation du Resting-state
    template=#14#;
    clear matlabbatch
    matlabbatch{1}.spm.spatial.normalise.estwrite.subj.source = cellstr(pinfo.t1.files);
    matlabbatch{1}.spm.spatial.normalise.estwrite.subj.wtsrc = '';
	temp = cellstr(pinfo.rest.realign.files);
	temp{end+1} = pinfo.t1.files;
	temp{end+1} = pinfo.t1.segment.gm;
	temp{end+1} = pinfo.t1.segment.wm;
	temp{end+1} = pinfo.t1.segment.csf;
    matlabbatch{1}.spm.spatial.normalise.estwrite.subj.resample = temp;
    [~, pmname, ext] = fileparts('#11#');
    if(template==0)
        matlabbatch{1}.spm.spatial.normalise.estwrite.eoptions.template = {[path2job filesep pmname ext]}; %utilisteur
    else
        matlabbatch{1}.spm.spatial.normalise.estwrite.eoptions.template = {[spm('Dir') '\templates\T1.nii,1']}; %utilisteur
    end
    matlabbatch{1}.spm.spatial.normalise.estwrite.eoptions.weight = '';
    matlabbatch{1}.spm.spatial.normalise.estwrite.eoptions.smosrc = 8;
    matlabbatch{1}.spm.spatial.normalise.estwrite.eoptions.smoref = 0;
    matlabbatch{1}.spm.spatial.normalise.estwrite.eoptions.regtype = 'mni';
    matlabbatch{1}.spm.spatial.normalise.estwrite.eoptions.cutoff = 25;
    matlabbatch{1}.spm.spatial.normalise.estwrite.eoptions.nits = 16;
    matlabbatch{1}.spm.spatial.normalise.estwrite.eoptions.reg = 1;
    matlabbatch{1}.spm.spatial.normalise.estwrite.roptions.preserve = 0;
    matlabbatch{1}.spm.spatial.normalise.estwrite.roptions.bb = [-78 -112 -50
        78 76 85];
    matlabbatch{1}.spm.spatial.normalise.estwrite.roptions.vox = [#12#];%utilisteur
    matlabbatch{1}.spm.spatial.normalise.estwrite.roptions.interp = #13#;%utilisteur
    matlabbatch{1}.spm.spatial.normalise.estwrite.roptions.wrap = [0 0 0];
    matlabbatch{1}.spm.spatial.normalise.estwrite.roptions.prefix = 'w';
    spm_jobman('run',matlabbatch);
    if(reorientationFlag==0 || reorientationFlag==2)
        pinfo.rest.norm.files = spm_select('FPList',pinfo.rest.dir,['^wuao_' pinfo.name '.*.nii']);
    else
        pinfo.rest.norm.files = spm_select('FPList',pinfo.rest.dir,['^wua' pinfo.name '.*.nii']);
    end
    
    if(isempty(pinfo.rest.norm.files))
        error(['Empty rest.norm.files files for ' pinfo.name]);
    end
    
    %% Smooth Resting-state
    clear matlabbatch
    matlabbatch{1}.spm.spatial.smooth.data = cellstr(pinfo.rest.norm.files);
    matlabbatch{1}.spm.spatial.smooth.fwhm = [#10#]; %utilisateur
    matlabbatch{1}.spm.spatial.smooth.dtype = 0;
    matlabbatch{1}.spm.spatial.smooth.im = 0;
    matlabbatch{1}.spm.spatial.smooth.prefix = 's';
    spm_jobman('run',matlabbatch);
    if(reorientationFlag==0 || reorientationFlag==2)
        pinfo.rest.smoothnorm.files = spm_select('FPList',pinfo.rest.dir,['^swuao_' pinfo.name '.*.nii']);
    else
        pinfo.rest.smoothnorm.files = spm_select('FPList',pinfo.rest.dir,['^swua' pinfo.name '.*.nii']);
    end
    
    if(isempty(pinfo.rest.smoothnorm.files))
        error(['Empty rest.smoothnorm.files files for ' pinfo.name]);
    end
    
    save([pinfo.dir '/pinfo.mat'],'pinfo');
catch exception
    disp(exception.message);
    quit;
end
quit;
