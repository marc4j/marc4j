package org.marc4j.marcxml;

import java.io.Reader;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import javax.xml.transform.Source;
import org.marc4j.MarcReader;

/**
 * <p>MarcSource</p>
 *
 * @author Bas Peters
 */
public class MarcSource implements Source {

    public static final String FEATURE =
	"http://org.marc4j.marcxml.MarcSource/feature";

    private String publicId = null;
    private String systemId = null;
    private InputStream	inputStream = null;
    private Reader reader = null;
    private MarcReader marcReader = null;

    public MarcSource() {}

    public MarcSource(File file) {
	setSystemId(file);
    }

    public MarcSource(MarcReader marcReader, File file) {
	setMarcReader(marcReader);
	setSystemId(file);
    }

    public MarcSource(InputStream stream) {
	this.inputStream = stream;
    }

    public MarcSource(MarcReader marcReader, InputStream stream) {
	this.inputStream = stream;
	setMarcReader(marcReader);
    }

    public MarcSource(InputStream stream, String systemID) {
	this.inputStream = stream;
	this.systemId = systemID;
    }

    public MarcSource(Reader reader) {
	this.reader = reader;
    }

    public MarcSource(MarcReader marcReader, Reader reader) {
	this.reader = reader;
	setMarcReader(marcReader);
    }

    public MarcSource(Reader reader, String systemID) {
	this.reader = reader;
	this.systemId = systemID;
    }

    public MarcSource(String systemID) {
	this.systemId = systemID;
    }

    public InputStream getInputStream() {
	return inputStream;
    }

    public String getPublicId() {
	return publicId;
    }

    public Reader getReader() {
	return reader;
    }

    public String getSystemId() {
	return systemId;
    }

    public MarcReader getMarcReader() {
	return marcReader;
    }

    public void setMarcReader(MarcReader marcReader) {
	this.marcReader = marcReader;
    }

    public void setInputStream(InputStream stream) {
	this.inputStream = stream;
    }

    public void setPublicId(String publicID) {
	this.publicId = publicID;
    }

    public void setReader(Reader reader) {
	this.reader = reader;
    }

    public void setSystemId(File file) {
	this.systemId = file.getAbsolutePath();
    }

    public void setSystemId(String systemID) {
	this.systemId = systemID;
    }
    
}

