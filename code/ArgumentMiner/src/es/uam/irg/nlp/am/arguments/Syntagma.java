/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.irg.nlp.am.arguments;

/**
 *
 * @author ansegura
 */
public class Syntagma {

    // Class members
    public int depth;
    public String function;
    public String text;

    /**
     * Class constructor.
     *
     * @param text
     * @param function
     * @param depth
     */
    public Syntagma(String text, String function, int depth) {
        this.text = text;
        this.function = function;
        this.depth = depth;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return String.format("%s > %s [%s]", this.text, this.function, this.depth);
    }
}
