package com.rem.otl.pc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.rem.core.Hub;
import com.rem.core.IFileManager;
import com.rem.core.storage.Resource;
import com.rem.core.storage.Storage;
import com.rem.core.Action;
import com.rem.otl.pc.gui.graphics.R;

public class FileManager implements IFileManager{
	@SuppressWarnings("rawtypes")
	@Override
	public Resource createImageResource(String name, String path) {
		return new Resource<InputStream>(name,path, R.getResource(path));
	}

	@Override
	public Resource<InputStream> createInputStream(String path,final int pathType) {
		if(path==null)return null;
		else {
			if(pathType==IFileManager.RELATIVE){
				path = new File(".",path).getAbsolutePath();
			}
			return new Resource<InputStream>(
					Storage.getMapNameFromFileName(path),
					path,
					Resource.INPUT_STREAM,
					new Action<Resource<InputStream>>(){
						@Override
						public void act(Resource<InputStream> resource){
							if(pathType==IFileManager.FROM_IMAGE_RESOURCE){
								InputStream is = R.getResource(resource.getPath());
								resource.set(is);
								return;
							}
							try {
								resource.set(new FileInputStream(/*new File("").getAbsolutePath()+File.separator+*/resource.getPath()));
							} catch (FileNotFoundException e) {
								File file = new File(resource.getPath());
								try {
									if(	file.createNewFile() ){
										resource.set(new FileInputStream(resource.getPath()));
										resource.setExists(false);
									}
								} catch (IOException e1) {
									Hub.log.debug("FileManager.createInputStream", resource.getPath());
									e1.printStackTrace();
								}
							}
						}
					});


		}
	}
	@Override
	public Resource<OutputStream> createOutputStream(String path, int pathType) {
		if(path==null)return null;
		if(pathType==IFileManager.RELATIVE){
			path = new File(".",path).getAbsolutePath();
		}
		return new Resource<OutputStream>(
				Storage.getMapNameFromFileName(path),
				path,
				Resource.OUTPUT_STREAM,
				new Action<Resource<OutputStream>>(){
					@Override
					public void act(Resource<OutputStream> resource){
						try {
							resource.set(new FileOutputStream(resource.getPath()));
						} catch (FileNotFoundException e) {
							File file = new File(resource.getPath());
							try {
								if(	file.createNewFile() ){
									resource.set(new FileOutputStream(resource.getPath()));
									resource.setExists(false);
								}
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
				});
	}

	@Override
	public boolean deleteFile(String path){
		File file = new File(path);
		if(file.exists()){
			return file.delete();
		}
		return false;
	}

	@Override
	public void createDirectory(String path){
		File file = new File(path);
		if(!file.exists()){
			file.mkdir();
		}
	}

	@Override
	public File getDirectory(String path) {
		return new File(path);
	}

}
