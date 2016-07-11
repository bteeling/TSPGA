//**************************************************************************************************************************
//**************************************************************************************************************************
//Class:		ImageConstruction
//Description:	Represent an image in a 2D array. Allows use of x,y coordinates and converts to r,c coordinates. Draws points.
//				lines, boxes, and circles.
//Author:		Steve Donaldson
//Date:			10/22/08
public class ImageConstruction {
	public int imageHeight;						//image height in pixel rows
	public int imageWidth;						//image width in pixel columns
	public int imageHeightMinus1;
	public int imageWidthMinus1;
	public double xLeft, xRight, yBottom, yTop;	//x,y coordinates of sides of rectangle matching visible drawing area
	public double xRange, yRange;
	public int redValues[][];					//the constructed image
	public int greenValues[][];
	public int blueValues[][];
	public int scaleFactor;						//amount by which to enlarge the image when displaying (>=1)
	public Display2DArray imageOut;				//used to show the image on screen
	public double epsilon;						//used to decide if a line should be considered vertical
	//**********************************************************************************************************************
	//Method:		ImageConstruction
	//Description:	Constructor to set up parameters for an image
	//Parameters:	height		- base image height (in x,y coordinate system)
	//				width		- base image width (in x,y coordinate system)
	//				left		- x,y coordinates of sides of rectangle matching visible drawing area.
	//				right		-		It is assumed that right>left & top>bottom.
	//				bottom		-
	//				top			- 
	//				scale		- the object scaleFactor
	//Returns:		nothing
	//Calls:		nothing
	ImageConstruction(int height, int width, double left, double right, double bottom, double top, int scale) {
		if (height <= 0)	//default
			height = 500;
		if (width <= 0)
			width = 500;
		imageHeight = height;
		imageWidth = width;
		imageHeightMinus1 = imageHeight - 1;
		imageWidthMinus1 = imageWidth - 1;
		xLeft = left;
		xRight = right;
		yBottom = bottom;
		yTop = top;
		xRange = xRight - xLeft;				//assumes xRight>xLeft & yTop>yBottom
		yRange = yTop - yBottom;
		redValues = new int[imageHeight][imageWidth];
		greenValues = new int[imageHeight][imageWidth];
		blueValues = new int[imageHeight][imageWidth];
		scaleFactor = scale;
		imageOut = null;
		epsilon = xRange * 1.0 / imageWidth;	//if the x coordinate of the endpoints of a line are within this range of
												//each other, the line is considered to be vertical
	}
	//**********************************************************************************************************************
	//Method:		insertLine
	//Description:	Inserts a straight line into a set of color arrays for subsequent output
	//				in a graphics window. Maps from (x,y) system to (r,c) system (i.e.,
	//				based on (r,c) origin at upper left of screen with columns increasing
	//				to the right and rows increasing down)
	//Parameters:	x1, y1, x2, y2   -	coordinates of the endpoints of the line to be drawn
	//				red, green, blue -	color values (0-255) for the line
	//Returns:		nothing
	//Calls:		nothing
	public void insertLine(double x1, double y1, double x2, double y2, int red, int green, int blue) {
		int outputRow, outputColumn;
		double x, y, xIncrement, yIncrement, slope, yIntercept;
		double horizontalPixels = Math.round((double)imageWidth / (double)xRange * Math.abs(x2 - x1));
		double verticalPixels = Math.round((double)imageHeight / (double)yRange * Math.abs(y2 - y1));
		double pixelsInLine = Math.round(Math.sqrt(horizontalPixels * horizontalPixels + verticalPixels * verticalPixels));

		if (Math.abs(x2 - x1) < epsilon)					//if x1 and x2 are within epsilon of each other...
			x2 = x1;										//then consider the line vertical

		if (x2 != x1) {		//non-vertical line
			slope = (y2 - y1) / (x2 - x1);
			yIntercept = y1 - slope * x1;
			if (x2 < x1) {
				x = x1;
				x1 = x2;
				x2 = x;
			}
			xIncrement = .9 * (x2 - x1) / pixelsInLine;		//multiply by .9 to guarantee no holes in line
			for (x = x1; x <= x2; x += xIncrement) {
				y = slope * x + yIntercept;
				if ((x >= xLeft) && (x <= xRight) && (y >= yBottom) && (y <= yTop)) {
					outputColumn = (int)Math.round(((-(double)xLeft + x) * (double)imageWidth / (double)xRange));
					outputRow = imageHeight - (int)Math.round(((-(double)yBottom + y) * (double)imageHeight / (double)yRange));
					if (outputColumn > imageWidthMinus1)
						outputColumn = imageWidthMinus1;
					if (outputRow > imageHeightMinus1)
						outputRow = imageHeightMinus1;
					redValues[outputRow][outputColumn] = red;
					greenValues[outputRow][outputColumn] = green;
					blueValues[outputRow][outputColumn] = blue;
				}
			}
		}
		else {				//vertical line
			if ((x1 < xLeft) || (x2 > xRight))
				return;
			if (y2 < y1) {
				y = y1;
				y1 = y2;
				y2 = y;
			}
			yIncrement = .9 * (y2 - y1) / pixelsInLine;	//multiply by .9 to guarantee no holes in line
			outputColumn = (int)Math.round(((-(double)xLeft + x1) * (double)imageWidth / (double)xRange));
			if (outputColumn > imageWidthMinus1)
				outputColumn = imageWidthMinus1;
			for (y = y1; y <= y2; y += yIncrement) {
                            //System.out.println("loop running");
                            //System.out.println("yincrement " + yIncrement);
				if ((y >= yBottom) && (y <= yTop)) {
					outputRow = imageHeight - (int)Math.round(((-(double)yBottom + y) * (double)imageHeight / (double)yRange));
					if (outputRow > imageHeightMinus1)
						outputRow = imageHeightMinus1;
					redValues[outputRow][outputColumn] = red;
					greenValues[outputRow][outputColumn] = green;
					blueValues[outputRow][outputColumn] = blue;
				}
			}
		}
	}
	//**********************************************************************************************************************
	//Method:		insertPoint
	//Description:	Inserts a single point into a set of color arrays for subsequent output
	//				in a graphics window. Maps from (x,y) system to (r,c) system (i.e.,
	//				based on (r,c) origin at upper left of screen with columns increasing
	//				to the right and rows increasing down)
	//Parameters:	x, y		     -	coordinates of the point to be drawn
	//				red, green, blue -	color values (0-255) for the line
	//Returns:		nothing
	//Calls:		nothing
	public void insertPoint(double x, double y, int red, int green, int blue) {
		if ((x >= xLeft) && (x <= xRight) && (y >= yBottom) && (y <= yTop)) {
			int outputColumn = (int)Math.round(((-(double)xLeft + x) * (double)imageWidth / (double)xRange));
			int outputRow = imageHeight - (int)Math.round(((-(double)yBottom + y) * (double)imageHeight / (double)yRange));
			if (outputColumn > imageWidthMinus1)
				outputColumn = imageWidthMinus1;
			if (outputRow > imageHeightMinus1)
				outputRow = imageHeightMinus1;
			redValues[outputRow][outputColumn] = red;
			greenValues[outputRow][outputColumn] = green;
			blueValues[outputRow][outputColumn] = blue;
		}
	}
	//**********************************************************************************************************************
	//Method:		insertBox
	//Description:	Inserts a box into a set of color arrays for subsequent output
	//				in a graphics window. Maps from (x,y) system to (r,c) system (i.e.,
	//				based on (r,c) origin at upper left of screen with columns increasing
	//				to the right and rows increasing down)
	//Parameters:	x1, y1, x2, y2		- coordinates of two opposite corners of the box to be drawn
	//				red, green, blue	- color values (0-255) for the box
	//				fill				- true=fill the box with the specified color; false=don't
	//Returns:		nothing
	//Calls:		nothing
	public void insertBox(double x1, double y1, double x2, double y2, int red, int green, int blue, boolean fill) {
		if (!fill) {
			insertLine(x1, y2, x2, y2, red, green, blue);			//top
			insertLine(x1, y1, x2, y1, red, green, blue);			//bottom
			insertLine(x1, y2, x1, y1, red, green, blue);			//left
			insertLine(x2, y2, x2, y1, red, green, blue);			//right
		}
		else {
			double y = y1;
			if (y1 > y2) {
				y1 = y2;
				y2 = y;
			}
			double verticalPixels = Math.round((double)imageHeight / (double)yRange * Math.abs(y2 - y1));
			double yIncrement = .9 * (y2 - y1) / verticalPixels;	//multiply by .9 to guarantee no holes in line
			for (y = y1; y <= y2; y += yIncrement)					//fill box from bottom to top with horizontal lines
				insertLine(x1, y, x2, y, red, green, blue);
		}
	}
	//**********************************************************************************************************************
	//Method:		insertCircle
	//Description:	Inserts a circle into a set of color arrays for subsequent output
	//				in a graphics window. Maps from (x,y) system to (r,c) system (i.e.,
	//				based on (r,c) origin at upper left of screen with columns increasing
	//				to the right and rows increasing down)
	//Parameters:	centerX, centerY	- coordinates of the center of the circle to be drawn
	//				radius				- radius of the circle (assumed > 0; no checking is performed for this)
	//				red, green, blue	- color values (0-255) for the circle
	//				fill				- true=fill the circle with the specified color; false=don't
	//Returns:		nothing
	//Calls:		insertPoint, insertLine
	public void insertCircle(double centerX, double centerY, double radius, int red, int green, int blue, boolean fill) {
		double x, y;
		double sizeOfVerticalPixel = yRange / imageHeight;
		double sizeOfHorizontalPixel = xRange / imageWidth;
		double sizeOfPixel = Math.min(sizeOfVerticalPixel, sizeOfHorizontalPixel);
		double angleBetweenPixels = Math.acos(1.0 - sizeOfPixel * sizeOfPixel / (2 * radius * radius));//from law of cosines
		if (fill)
			angleBetweenPixels *= .5;	//to help avoid gaps in filled image
		for (double angle = 0.0; angle < 2 * Math.PI; angle += angleBetweenPixels) {
			x = centerX + radius * Math.cos(angle);
			y = centerY + radius * Math.sin(angle);
                        //System.out.println("centerx " + centerX + " centerY " + centerY + " x " + x + " y " + y);
			if (fill)
				insertLine(centerX, centerY, x, y, red, green, blue);
			else
				insertPoint(x, y, red, green, blue);
		}
	}
	//**********************************************************************************************************************
	//Method:		insertText
	//Description:	Inserts text into the Display2DArray object imageOut at a specified x,y coordinate position for
	//				subsequent output in in a graphics window. Maps from (x,y) system to (r,c) system (i.e., based on (r,c)
	//				origin at upper left of screen with columns increasing to the right and rows increasing down)
	//Parameters:	x, y		- coordinates of the position at which the text is to be inserted
	//				textToShow	- the text to be shown in the graphics window
	//				textRow		- row in the text array in the Display2DArray object at which textToShow is to be placed
	//								(and the row in the coordinates array for the outputRow and outputColumn values)
	//Returns:		nothing
	//Calls:		nothing
	public void insertText(double x, double y, String textToShow, int textRow) {
            //System.out.println(textToShow);
            //System.out.println(x + " " + y);
		if ((x >= xLeft) && (x <= xRight) && (y >= yBottom) && (y <= yTop)) {
			int outputColumn = (int)Math.round(((-(double)xLeft + x) * (double)imageWidth / (double)xRange));
			int outputRow = imageHeight - (int)Math.round(((-(double)yBottom + y) * (double)imageHeight / (double)yRange));
			if (outputColumn > imageWidthMinus1)
				outputColumn = imageWidthMinus1;
			if (outputRow > imageHeightMinus1)
				outputRow = imageHeightMinus1;
			imageOut.text[textRow] = textToShow;
			imageOut.textPosition[textRow][0] = outputRow;
			imageOut.textPosition[textRow][1] = outputColumn;
		}
	}
	//**********************************************************************************************************************
	//Method:		clearImage
	//Description:	Sets color arrays for a graphics image to a specified background color.
	//Parameters:	red, green, blue - color values for the background
	//Returns:		nothing
	//Calls:		nothing
	public void clearImage(int red, int green, int blue) {
		for (int r = 0; r < imageHeight; r++)
			for (int c = 0; c < imageWidth; c++) {
				redValues[r][c] = red;
				greenValues[r][c] = green;
				blueValues[r][c] = blue;
			}
	}
	//**********************************************************************************************************************
	//Method:		displaySetup
	//Description:	sets up a Display2dArray object to be used for displaying the (possibly enlarged) image
	//Parameters:	none
	//Returns:		nothing but creates and updates imageOut
	//Calls:		setPixelValues
	//				Display2DArray constructor in class Display2DArray
	public void displaySetup() {
		int scaledImageHeight = imageHeight * scaleFactor;
		int scaledImageWidth = imageWidth * scaleFactor;
		int newRedValues[][] = new int[scaledImageHeight][scaledImageWidth];
		int newGreenValues[][] = new int[scaledImageHeight][scaledImageWidth];
		int newBlueValues[][] = new int[scaledImageHeight][scaledImageWidth];
		imageOut = new Display2DArray(1, scaledImageWidth, scaledImageHeight, scaledImageHeight * scaledImageWidth, newRedValues, newGreenValues, newBlueValues, null, null);
		setPixelValues();
	}
	//**********************************************************************************************************************
	//Method:		setPixelValues
	//Description:	Sets the pixel values in the Display2DArray object used for displaying the image
	//Parameters:	none
	//Returns:		nothing but updates imageOut
	//Calls:		nothing
	public void setPixelValues() {
		//no scaling required
		if (scaleFactor == 1) {
			imageOut.redPixels = redValues;
			imageOut.greenPixels = greenValues;
			imageOut.bluePixels = blueValues;
		}
		//scale (enlarge) the image
		else {
			int row, column;				//row,column in ImageConstruction object being mapped to Display2DArray object
			int newRowStart, newColumnStart;//starting row and col positions in Display2DArray object being mapped to
			int r, c;						//row,column positions in Display2DArray object being mapped to from
											//ImageConstruction object

			newRowStart = 0;
			for (row = 0; row < imageHeight; row++) {
				newColumnStart = 0;
				for (column = 0; column < imageWidth; column++) {
					for (r = newRowStart; r < newRowStart + scaleFactor; r++) {	//set all pixels in block to same color
						for (c = newColumnStart; c < newColumnStart + scaleFactor; c++) {
							imageOut.redPixels[r][c] = redValues[row][column];
							imageOut.greenPixels[r][c] = greenValues[row][column];
							imageOut.bluePixels[r][c] = blueValues[row][column];
						}
					}
					newColumnStart += scaleFactor;
				}
				newRowStart += scaleFactor;
			}
		}
	}
	//**********************************************************************************************************************
	//Method:		displayImage
	//Description:	Displays the constructed image
	//Parameters:	performDisplaySetup	- true=call displaySetup method; false=don't. Typically, for a given
	//										ImageConstruction object, displayImage will be called the first time with
	//										this parameter set to true and then subsequently (if needed) with it set to
	//										false (for proper display results). Otherwise the final window displayed may not
	//										clear even after a call to closeDisplay.
	//										Note, also, that setPixelValues should typically probably be called before an
	//										image is actually displayed but that this occurs automatically if displaySetup
	//										is called.
	//				windowTitle			- text for the display window
	//				showOneTimeOnly		- true=show the image one time; false=continuously
	//Returns:		nothing
	//Calls:		displaySetup
	//				showImage in class Display2DArray
	public void displayImage(boolean performDisplaySetup, String windowTitle, boolean showOneTimeOnly) {
		if(performDisplaySetup)
			displaySetup();
		imageOut.showImage(windowTitle, showOneTimeOnly);
	}
	//**********************************************************************************************************************
	//Method:		closeDisplay
	//Description:	Closes the window in which the image is displayed
	//Parameters:	none
	//Returns:		nothing
	//Calls:		closeImageDisplay in class Display2DArray
	public void closeDisplay() {
		imageOut.closeImageDisplay();
	}
	//**********************************************************************************************************************

	//Method:		insertSpecialLine
	//Description:	Works almost like insertLine except increments the color array values (corresponding to the line) by a
	//				specified amount rather than assigning them actual color values. This provides a means of highlighting
	//				only those points that are repeatedly accessed (perhaps because they represent intersctions of multiple
	//				lines, etc.).
	//Parameters:	x1, y1, x2, y2  - coordinates of the endpoints of the line to be drawn
	//				colorIncrement	- the amount to add to the current value at each pixel position in each of the color
	//									arrays at those array locations corresponding to the line being drawn.
	//Returns:		nothing
	//Calls:		nothing
	public void insertSpecialLine(double x1, double y1, double x2, double y2, int colorIncrement) {
		int outputRow, outputColumn, previousOutputRow = -1, previousOutputColumn = -1;
		double x, y, xIncrement, yIncrement, slope, yIntercept;
		double horizontalPixels = Math.round((double)imageWidth / (double)xRange * Math.abs(x2 - x1));
		double verticalPixels = Math.round((double)imageHeight / (double)yRange * Math.abs(y2 - y1));
		double pixelsInLine = Math.round(Math.sqrt(horizontalPixels * horizontalPixels + verticalPixels * verticalPixels));

		if (Math.abs(x2 - x1) < epsilon)					//if x1 and x2 are within epsilon of each other...
			x2 = x1;										//then consider the line vertical

		if (x2 != x1) {		//non-vertical line
			slope = (y2 - y1) / (x2 - x1);
			yIntercept = y1 - slope * x1;
			if (x2 < x1) {
				x = x1;
				x1 = x2;
				x2 = x;
			}
			xIncrement = .9 * (x2 - x1) / pixelsInLine;		//multiply by .9 to guarantee no holes in line
			for (x = x1; x <= x2; x += xIncrement) {
				y = slope * x + yIntercept;
				if ((x >= xLeft) && (x <= xRight) && (y >= yBottom) && (y <= yTop)) {
					outputColumn = (int)Math.round(((-(double)xLeft + x) * (double)imageWidth / (double)xRange));
					outputRow = imageHeight - (int)Math.round(((-(double)yBottom + y) * (double)imageHeight / (double)yRange));
					if (outputColumn > imageWidthMinus1)
						outputColumn = imageWidthMinus1;
					if (outputRow > imageHeightMinus1)
						outputRow = imageHeightMinus1;
					//don't duplicate pixels in the this line
					if ((outputRow != previousOutputRow) || (outputColumn != previousOutputColumn)) {
						redValues[outputRow][outputColumn] += colorIncrement;
						greenValues[outputRow][outputColumn] += colorIncrement;
						blueValues[outputRow][outputColumn] += colorIncrement;
						previousOutputRow = outputRow;
						previousOutputColumn = outputColumn;
					}
				}
			}
		}
		else {				//vertical line
			if ((x1 < xLeft) || (x2 > xRight))
				return;
			if (y2 < y1) {
				y = y1;
				y1 = y2;
				y2 = y;
			}
			yIncrement = .9 * (y2 - y1) / pixelsInLine;	//multiply by .9 to guarantee no holes in line
			outputColumn = (int)Math.round(((-(double)xLeft + x1) * (double)imageWidth / (double)xRange));
			if (outputColumn > imageWidthMinus1)
				outputColumn = imageWidthMinus1;
			for (y = y1; y <= y2; y += yIncrement) {
				if ((y >= yBottom) && (y <= yTop)) {
					outputRow = imageHeight - (int)Math.round(((-(double)yBottom + y) * (double)imageHeight / (double)yRange));
					if (outputRow > imageHeightMinus1)
						outputRow = imageHeightMinus1;
					//don't duplicate pixels in the this line
					if ((outputRow != previousOutputRow) || (outputColumn != previousOutputColumn)) {
						redValues[outputRow][outputColumn] += colorIncrement;
						greenValues[outputRow][outputColumn] += colorIncrement;
						blueValues[outputRow][outputColumn] += colorIncrement;
						previousOutputRow = outputRow;
						previousOutputColumn = outputColumn;
					}
				}
			}
		}
	}
	//**********************************************************************************************************************
}
//**************************************************************************************************************************
//**************************************************************************************************************************