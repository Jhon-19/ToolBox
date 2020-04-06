package notes;

import org.litepal.crud.LitePalSupport;

import java.util.Date;

public class Note extends LitePalSupport {

	private String text;
	private String title;
	private Date created_time;
	private Date last_edited_time;
	private int length;
	private int id;

	public String getTitle() {
		return title;
	}
	
	public String getText() {
		return text;
	}
	
	public int getLength(){
		return length;
	}
	
	public int getId() {
		return id;
	}
	
	public Date getCreatedTime() {
		return created_time;
	}
	
	public Date getLastEditedTime() {
		return last_edited_time;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setCreated_time(Date created_time) {
		this.created_time = created_time;
	}

	public void setLast_edited_time(Date last_edited_time) {
		this.last_edited_time = last_edited_time;
	}

	public void setLength(int length) {
		this.length = length;
	}

	@Override
	public String toString(){
		return title;
	}
}
