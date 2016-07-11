
import java.util.Arrays;

//**************************************************************************************************************************
//**************************************************************************************************************************
//Class:	Member
//Description:	Provides methods for creating and manipulating Member objects
//Author:	Brandon Teeling
//Date:		03/1/16
public class Member {

    private City[] genome;
    private double distance;
    //private double fitness;

    //private City[] cities;
    //**************************************************************************
    //Method:      Member
    //Description: Constructor which creates a Member  object.
    //Parameters:  genome Contains the cities in order which they will be visited.
    //Returns:     None
    //Calls:       computeMemberDistance, computeFitness
    //Globals:     genome, distance, fitness

    public Member(City[] genome) {
        this.genome = genome;
        this.distance = computeMemberDistance();
        //this.fitness = 0;//computeFitness();
    }//end of method

    public void displayMember() {
        for (int i = 0; i < genome.length; i++) {
            System.out.print(genome[i].getLabel() + " ");  
        }//end of loop
        System.out.println("\nTotal Distance: " + distance);// + " Fitness: " + fitness + "");
        //System.out.println("------------------------------------------------------------------------------");
    }//end of method
    //**************************************************************************
    //Method:      getGenome
    //Description: Returns the genome for the member.
    //Parameters:  None
    //Returns:     City[] Array of cities representing the genome.
    //Calls:       None
    //Globals:     genome

    public City[] getGenome() {
        return genome;
    }//end of method
    
    //**************************************************************************
    //Method:      getDistance
    //Description: Returns the total distance for the member.
    //Parameters:  None
    //Returns:     double indicating the distance.
    //Calls:       None
    //Globals:     distance

    public double getDistance() {
        return distance;
    }//end of method
    //**************************************************************************
    //Method:      computeMemberDistance
    //Description: Computes the total distance for the member.
    //Parameters:  None
    //Returns:     double indicating the distance.
    //Calls:       computeCityDistance
    //Globals:     genome

    private double computeMemberDistance() {
        double total = 0;
        for (int i = 0; i < genome.length - 1; i++) {
            total += computeCityDistance(genome[i], genome[i + 1]);
            //System.out.println("city " + genome[i].getLabel() + " city " + genome[i + 1].getLabel());
            //System.out.println("current total is " + total);
        }//end of loop
        return total;
    }//end of method

    //private double computeFitness() {
     //   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   // }//enbd of method
    //**************************************************************************
    //Method:      computeCityDistance
    //Description: Computes the distance between two cities.
    //Parameters:  city1 Starting city.
    //             city2 Ending city.
    //Returns:     double indicating the distance.
    //Calls:       None
    //Globals:     None

    private double computeCityDistance(City city1, City city2) {
        double x1 = city1.getX(), x2 = city2.getX(), y1 = city1.getY(), y2 = city2.getY();
        return Math.sqrt((Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2)));
    }//end of method
}//end of class
