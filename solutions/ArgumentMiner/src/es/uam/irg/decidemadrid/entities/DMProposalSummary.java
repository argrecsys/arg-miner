/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.decidemadrid.entities;

/**
 *
 * @author ansegura
 */
public class DMProposalSummary {
    
    private String categories;
    private String date;
    private String districts;
    private int id;
    private String title;
    private String topics;

    public DMProposalSummary(int id, String title, String date, String categories, String districts, String topics) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.categories = categories;
        this.districts = districts;
        this.topics = topics;
    }
    
    public String getCategories() {
        return this.categories;
    }


    public String getDate() {
        return date;
    }
    
    public String getDistricts() {
        return this.districts;
    }
    
    public int getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }

    public String getTopics() {
        return this.topics;
    }
    
}
