//**************************************************************************************************************************
//**************************************************************************************************************************
//Class:	City
//Description:	Provides methods for creating and manipulating City objects
//Author:	Brandon Teeling
//Date:		03/1/16

public class City {
    private int label;
    private double x;
    private double y;
    //**************************************************************************
    //Method:      City
    //Description: Constructor which creates a city object
    //Parameters:  label Indicates the number to assign to a city
    //             x     Indicates the x coordinate of the city
    //             y     Indicates the y coordinate of the city
    //Returns:     None
    //Calls:       None
    //Globals:     label, x, y
    
    public City(int label, double x, double y) {
        this.label = label;
        this.x = x;
        this.y = y;
    }//end of method
    
    public double getX() {
        return x;
    }//end of method

    public void setX(double x) {
        this.x = x;
    }//end of method

    public double getY() {
        return y;
    }//end of method

    public void setY(double y) {
        this.y = y;
    }//end of method
    public int getLabel() {
        return label;
    }//end of method

    public void setLabel(int label) {
        this.label = label;
    }//end of method
}//end of class
