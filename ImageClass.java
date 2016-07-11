import java.io.*;

//Class:		ImageClass
//Description:	Parameters and data defining a graphics image and routines to set up those
//				parameters. The routines in this class are constructed to be as modular as
//				possible, thereby facilitating processing for both single image retrieval
//				from a file, repeated retrieval (as when the file is constantly being
//				written to by another program), or user created images. Instances of this
//				class can be displayed either by creating instances of the DisplayPixelData
//				class or the Display2DArray class.
//
//				A standard sequence of events might be:
//					Create an instance of this class, then for that object:
//						specifyImageFile()
//						readBasicImageParameters()		//e.g., perhaps for a color image...
//						getRawPixelData()
//						closeFile()
//						initializeImageArray()
//						setColorValues()
//						Do something to the color image to produce a new image array (e.g.,
//							produce a grayscale image form the color image)
//					Create a new instance of this class for the image just created,
//					then for that object:
//						setBasicParameters()
//						initializeImageArray()
//						setGrayValues()
//					Create an instance of Display2DArray based on the second instance of
//					this class, then for that object:
//						setGrayArrayData()
//						paint()
public class ImageClass {
	public String imageFileName;		//the name of a specified image file
	public RandomAccessFile imageFile;	//pointer to the specified file
	public int imageType;				//1=24 bit color;2=256 level gray scale;3=binary (b/w)
	public int imageWidth;				//image width in pixels
	public int imageHeight;				//image height in pixels
	public int imageSize;				//height*width (*3 for images of type 1)
	public byte rawPixelData[];			//typically read from file (may not be used if image
										//is not read from file)
	public boolean flipImage;			//true=flip the image; false=don't flip it. If
										//pixelData was derived from a .bmp image, it may
										//not yet have been flipped. For example,
										//BMPconversion.exe flips the .bmp image (i.e.,
										//turns it right side up) but eye.exe does not. If
										//this parameter is set to "true" paint() will
										//invert the image.
	public int pixels[][];				//pixel values in 2D for type 1 or type 2 images
	public int redPixels[][];			//red pixel values for type 1 images
	public int greenPixels[][];			//green pixel values for type 1 images
	public int bluePixels[][];			//blue pixel values for type 1 images
	//Note: pixels[][], redPixels[][], greenPixels[][], and bluePixels[][] may not be defined
	//for some tasks (e.g., if the only objective is to display data read from a file).
	//**************************************************************************************
	//Method:		ImageClass
	//Description:	Default constructor
	//Parameters:	none
	//Returns:		nothing
	//Calls:		nothing
	ImageClass() {
		imageFileName = "";
		imageFile = null;
		imageType = 0;
		imageWidth = 0;
		imageHeight = 0;
		imageSize = 0;
		flipImage = false;
		rawPixelData=null;
		pixels=null;
		redPixels = null;
		greenPixels = null;
		bluePixels = null;
	}
	//**************************************************************************************
	//Method:		ImageClass
	//Description:	Constructor to build one image object from another
	//Parameters:	imageObject - the source image object
	//Returns:		nothing
	//Calls:		nothing
	ImageClass(ImageClass imageObject) {
		imageFileName = imageObject.imageFileName;
		imageFile = imageObject.imageFile;
		imageType = imageObject.imageType;
		imageWidth = imageObject.imageWidth;
		imageHeight = imageObject.imageHeight;
		imageSize = imageObject.imageSize;
		flipImage = imageObject.flipImage;
		rawPixelData = imageObject.rawPixelData;
		pixels = imageObject.pixels;
		redPixels = imageObject.redPixels;
		greenPixels = imageObject.greenPixels;
		bluePixels = imageObject.bluePixels;
	}
	//**************************************************************************************
	//Method:		loadAllImageData
	//Description:	Allows the user to specify a file containing an image, then retrieves
	//				the image data and loads the applicable arrrays. The file is not closed
	//				by this routine.
	//Parameters:	defaultFileName		- name used if the user does not specify a file	name
	//				defaultFlipStatus	- Y, N, or blank. If 'Y' or 'N' the object variable
	//										flipImage is set to true or false, respectively.
	//										If blank (or anything other than Y or N), the
	//										user is prompted as to whether or not the image
	//										is to be flipped (in the setColorValues() or
	//										setGrayValues() methods and flipImage is set
	//										accordingly. 
	//Returns:		true if successful; false otherwise
	//Calls:		specifyImageFile
	//				readBasicImageParameters
	//				getRawPixelData
	//				initializeImageArray
	//				setColorValues

	public boolean loadAllImageData(String defaultFileName, char defaultFlipStatus) {
		if (specifyImageFile(defaultFileName, defaultFlipStatus))
			if(readBasicImageParameters())
				if(getRawPixelData())
					if(initializeImageArray())
						if(imageType==1){
							if(setColorValues())
								return true;
						}
						else if((imageType==2)||(imageType==3)){
							if (setGrayValues())
								return true;
						}
		return false;
	}
	//**************************************************************************************
	//Method:		specifyImageFile
	//Description:	Lets the user specify a file containing an image of interest. An image
	//				in the file must be stored as follows:
	//				Type:		an integer value 1, 2, 3, where 1=24 bit color, 2=256 level
	//							gray scale, and 3=binary (black/white)
	//				Columns:	an integer giving the # of columns in the image
	//				Rows:		an integer giving the # of rows in the image
	//				Pixel data:	Each pixel value is stored in a single byte for types 2 and
	//							3, and in three bytes for type 1 (in the order B, G, R).
	//				The original image format from which this special format is created is
	//				typically .bmp. Depending upon the program that converted the .bmp image,
	//				the pixel data may or may not have already been inverted. For example, 
	//				BMPconversion.exe flips the .bmp image (i.e., turns it right side up) but
	//				eye.exe does not. Other routine(s) in this class can flip the pixels,
	//				if desired. 
	//Parameters:	defaultFileName		- name used if the user does not specify a file	name
	//				defaultFlipStatus	- T, F, or blank. If 'T' or 'F' the object variable
	//										flipImage is set to true or false, respectively.
	//										If blank (or anything other than T or F), the
	//										user is prompted as to whether or not the image
	//										is to be flipped (in the setColorValues() or
	//										setGrayValues() methods and flipImage is set
	//										accordingly. 
	//Returns:		true if successful; false otherwise
	//				Updates imageFileName and imageFile parameters for the calling object
	//Calls:		nothing
	public boolean specifyImageFile(String defaultFileName, char defaultFlipStatus) {
		String name,userInput;
		char dummy;
		RandomAccessFile file;
		KeyboardInputClass keyboardInput = new KeyboardInputClass();
		if (defaultFileName.length() != 0)
			name = defaultFileName;
		else
			name = keyboardInput.getKeyboardInput("Specify the file containing the image data: ");
		try {
			file = new RandomAccessFile(name, "r");
			imageFileName = name;			//update object parameter
			imageFile = file;				//update object parameter
			if ((defaultFlipStatus == 'Y')||(defaultFlipStatus == 'y'))
				flipImage=true;
			else if ((defaultFlipStatus == 'N')||(defaultFlipStatus == 'n'))
				flipImage = false;
			else {
				userInput = keyboardInput.getKeyboardInput("Flip the image? (Y/N) ");
				if (userInput.length() > 0) {
					dummy = userInput.charAt(0);
					if ((dummy=='Y')||(dummy=='y'))
						flipImage=true;
					else
						flipImage=false;
				}
			}
			return true;
		}
		catch (Exception e) {
			keyboardInput.getKeyboardInput("Problem trying to read file in method specifyImageFile. Press ENTER to continue...");
			return false;
		}
	}
	//**************************************************************************************
	//Method:		readBasicImageParameters
	//Description:	Reads type, width, and height data for the specified image file
	//Parameters:	none
	//Returns:		true if the retrieval operation was successful; false if it was not
	//				Updates imageType, imageWidth, imageHeight, imageSize, and the
	//				rawPixelData[][] array for the calling object, if the read is successful.
	//Calls:		initializeBasicParameters
	//				initializeRawPixelDataArray
	public boolean readBasicImageParameters() {
		char c1, c2, c3, c4;
		int type = 0, width = 0, height = 0;
		KeyboardInputClass keyboardInput = new KeyboardInputClass();
		if (imageFile == null) {
			keyboardInput.getKeyboardInput("No file specified. Press ENTER to continue...");
			return false;
		}

		try {
			imageFile.seek(0);			//move pointer to beginning of file
			//Read the file type, rows, and columns (stored as integers). This requires reading
			//four bytes per value. These bytes represent an integer stored by C++ or Basic
			//(i.e., in low byte to high byte order (not reversed bit order!)). The routine
			//converts to a Java integer representation (i.e., high byte to low byte order).
			c1 = (char)imageFile.read();
			c2 = (char)imageFile.read();
			c3 = (char)imageFile.read();
			c4 = (char)imageFile.read();
			type = (c4 << 24) | (c3 << 16) | (c2 << 8) | c1;
			c1 = (char)imageFile.read();
			c2 = (char)imageFile.read();
			c3 = (char)imageFile.read();
			c4 = (char)imageFile.read();
			width = (c4 << 24) | (c3 << 16) | (c2 << 8) | c1;
			c1 = (char)imageFile.read();
			c2 = (char)imageFile.read();
			c3 = (char)imageFile.read();
			c4 = (char)imageFile.read();
			height = (c4 << 24) | (c3 << 16) | (c2 << 8) | c1;
			setBasicParameters(type, width, height);		//update object parameters
			initializeRawPixelDataArray();					//initialize object parameter
			return true;
		}
		catch (Exception e) {
			keyboardInput.getKeyboardInput("Problem trying to read file in method readBasicImageParameters. Press ENTER to continue...");
		}
		return false;
	}
	//**************************************************************************************
	//Method:		getRawPixelData
	//Description:	Reads pixel data for the specified image
	//Parameters:	imageFile	- the identifier of the file containing the image data
	//Returns:		true if the retrieval operation was successful; false if it was not
	//				Updates rawPixelData for the calling object
	//Calls:		nothing
	public boolean getRawPixelData() {
		KeyboardInputClass keyboardInput = new KeyboardInputClass();
		if (imageFile == null) {
			keyboardInput.getKeyboardInput("No file specified. Press ENTER to continue..."); 
			return false;
		}
		try {
			int pixelDataStart = 12;				//skip imageType, width, and height data
			imageFile.seek(pixelDataStart);			//move to beginning of pixel data
			imageFile.read(rawPixelData, 0, imageSize);
			return true;
		}
		catch (Exception e) {
			keyboardInput.getKeyboardInput("Problem trying to read file in method getRawPixelData. Press ENTER to continue...");
		}
		return false;
	}
	//**************************************************************************************
	//Method:		closeFile
	//Description:	Closes the open image file
	//Parameters:	imageFile	- the identifier of the file containing the image data
	//Returns:		true if the close operation was successful; false if it was not
	//Calls:		nothing
	public boolean closeFile() {
		try {
			imageFile.close();
			return true;
		}
		catch (Exception e) {
			KeyboardInputClass keyboardInput = new KeyboardInputClass();
			keyboardInput.getKeyboardInput("Problem trying to close file. Press ENTER to abort...");
		}
		return false;
	}
	//**************************************************************************************
	//Method:		setBasicParameters
	//Description:	Sets the image type, width, height, and size parameters
	//Parameters:	type	- 1=24 bit color, 2=256 level gray scale, 3=binary (b/w)
	//				width	- image width in pixels
	//				height	- image height in pixels
	//Returns:		true (always successful)
	//				Updates object parameters imageType, imageWidth, imageHeight, imageSize
	//Calls:		nothing
	public boolean setBasicParameters(int type, int width, int height) {
		imageType = type;
		imageWidth = width;
		imageHeight = height;
		imageSize = imageHeight * imageWidth;
		if (imageType == 1)
			imageSize *= 3;
		return true;
	}
	//**************************************************************************************
	//Method:		initializeRawPixelDataArray
	//Description:	Allocates memory for the pixel data (typically read from a file)
	//Parameters:	none
	//Returns:		true if successful; false otherwise
	//Calls:		nothing
	public boolean initializeRawPixelDataArray() {
		if (imageSize>0){
			rawPixelData = new byte[imageSize];
			return true;
		}
		return false;
	}
	//**************************************************************************************
	//Method:		initializeImageArray
	//Description:	Sets up the 2D array(s) to hold an image
	//Parameters:	none
	//Returns:		true if the operation is successful; false otherwise
	//				Initializes redPixels[][], greenPixels[][], bluePixels[][] for the
	//				calling object if the image is in color, pixels[][] if the image is
	//				grayscale or binary.
	//Calls:		nothing
	public boolean initializeImageArray() {
		if (imageType == 1) {
			redPixels = new int[imageHeight][imageWidth];
			greenPixels = new int[imageHeight][imageWidth];
			bluePixels = new int[imageHeight][imageWidth];
			return true;
		}
		else if ((imageType == 2) || (imageType == 3)) {
			pixels = new int[imageHeight][imageWidth];
			return true;
		}
		return false;
	}
	//**************************************************************************************
	//Method:		setColorValues
	//Description:	Takes pixel values from the one-dimensional rawPixelData array and puts
	//				them into 2D arrays, one array each for red, green, and blue. Flips the
	//				image if desired.
	//Parameters:	none
	//Returns:		true if the operation is successful; false otherwise
	//				Updates redPixels[][], greenPixels[][], and bluePixels[][] for the
	//				calling object.
	//Calls:		nothing
	public boolean setColorValues() {
		if ((imageType != 1) || (imageSize == 0)) {
			KeyboardInputClass keyboardInput = new KeyboardInputClass();
			keyboardInput.getKeyboardInput("Incompatible parameters in method setColorValues. Press ENTER to continue..."); 
			return false;
		}
		int row, column, bmpRow;
		int heightMinusOne = imageHeight - 1;
		int currentItem = 0;
		for (bmpRow = 0; bmpRow < imageHeight; bmpRow++) {
			if (flipImage)
				row = heightMinusOne - bmpRow;		//flip image (from BMP format)
			else
				row = bmpRow;						//don't flip
			for (column = 0; column < imageWidth; column++) {
				if (rawPixelData[currentItem]<0)
					bluePixels[row][column] = 256+rawPixelData[currentItem];
				else
					bluePixels[row][column] = rawPixelData[currentItem];
				currentItem++;
				if (rawPixelData[currentItem] < 0)
					greenPixels[row][column] = 256 + rawPixelData[currentItem];
				else
					greenPixels[row][column] = rawPixelData[currentItem];
				currentItem++;
				if (rawPixelData[currentItem] < 0)
					redPixels[row][column] = 256 + rawPixelData[currentItem];
				else
					redPixels[row][column] = rawPixelData[currentItem];
				currentItem++;
			}
		}
		return true;
	}
	//**************************************************************************************
	//Method:		setGrayValues
	//Description:	Takes pixel values from the one-dimensional rawPixelData array and puts
	//				them into a 2D array for grayscale or binary images.
	//Parameters:	none
	//Returns:		true if the operation is successful; false otherwise
	//				Updates pixels[][] for the calling object
	//Calls:		nothing
	public boolean setGrayValues() {
		if ((imageType == 1) || (imageSize == 0)) {
			KeyboardInputClass keyboardInput = new KeyboardInputClass();
			keyboardInput.getKeyboardInput("Incompatible parameters in method setGrayValues. Press ENTER to continue...");
			return false;
		}
		int row, column, bmpRow;
		int heightMinusOne = imageHeight - 1;
		int currentItem = 0;
		for (bmpRow = 0; bmpRow < imageHeight; bmpRow++) {
			if (flipImage)
				row = heightMinusOne - bmpRow;		//flip image (from BMP format)
			else
				row = bmpRow;						//don't flip
			for (column = 0; column < imageWidth; column++) {
				if (rawPixelData[currentItem] < 0)
					pixels[row][column] = 256 + rawPixelData[currentItem];
				else
					pixels[row][column] = rawPixelData[currentItem];
				currentItem++;
			}
		}
		return true;
	}
	//**************************************************************************************
	//Method:		copyColorValues
	//Description:	Copies RGB pixel values from a specified set of arrays into the arrays
	//				for this image. Making a copy prevents a change to the source arrays
	//				from subsequently affecting this image.
	//Parameters:	redValues[][]	- source red pixels
	//				greenValues[][]	- source green pixels
	//				blueValues[][]	- source blue pixels
	//Returns:		true if successful; false otherwise
	//Calls:		nothing
	public boolean copyColorValues(int redValues[][], int greenValues[][], int blueValues[][]) {
		if ((redValues != null) && (greenValues != null) && (blueValues != null)) {
			redPixels = new int[imageHeight][imageWidth];
			greenPixels = new int[imageHeight][imageWidth];
			bluePixels = new int[imageHeight][imageWidth];
			for (int r = 0; r < imageHeight; r++)
				for (int c = 0; c < imageWidth; c++) {
					redPixels[r][c] = redValues[r][c];
					greenPixels[r][c] = greenValues[r][c];
					bluePixels[r][c] = blueValues[r][c];
				}
			return true;
		}
		return false;
	}
	//**************************************************************************************
	//Method:		copyGrayValues
	//Description:	Copies grayscale pixel values from a specified array into the array
	//				for this image. Making a copy prevents a change to the source array
	//				from subsequently affecting this image.
	//Parameters:	values[][]	- source gray pixels
	//Returns:		true if successful; false otherwise
	//Calls:		nothing
	public boolean copyGrayValues(int values[][]) {
		if (values != null) {
			pixels = new int[imageHeight][imageWidth];
			for (int r = 0; r < imageHeight; r++)
				for (int c = 0; c < imageWidth; c++)
					pixels[r][c] = values[r][c];
			return true;
		}
		return false;
	}
	//**************************************************************************************
}	//end ImageClass