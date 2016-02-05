package com.faceflag.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;

class FlagOverlay {

	private final int DEFAULT_CHEECK_RANGE=2;

	private int DEFAULT_GRADIENT_DECENT = 10;
	private int incrementalFactorX=6;
	private int incrementalFactorY=1;

    final private int DEFAULT_BOTTOM_BRIGHTNESS_LIMIT_OF_SKIN_COLOR= 50;
    final private int DEFAULT_TOP_BRIGHTNESS_LIMIT_OF_SKIN_COLOR= 255;

    final private int DEFAULT_EYE_OPENING_WIDTH=7;
    final private int DEFAULT_EYE_OPENING_HIEGHT=2;

    private double SD;
    private int[] rgb0;
    private double modulus0;

    final private double cheekWidthByTwo = DEFAULT_CHEECK_RANGE * incrementalFactorX;
    final private double cheekHeightByTwo = DEFAULT_CHEECK_RANGE * incrementalFactorY;

    private Bitmap face;
	private Bitmap flag;

	private int[] cheekLeft;
	private int[] cheekRight;

    private int[] eyeLeft;
    private int[] eyeRight;

    //public construction of Flag Overlay
    //
    //@params: face bitmap, flag bitmap, left cheek coordinates, right cheek coordinates, left eye coordinates, right eye coordinates
	public FlagOverlay( Bitmap face, Bitmap flag, int[] cheekLeft, int[] cheekRight,int[] eyeLeft,int[] eyeRight) {
		this.face = getNormalizedImage(face);
		this.flag = getNormalizedImage(flag);

		this.cheekLeft = cheekLeft;
		this.cheekRight = cheekRight;

        this.eyeLeft=getAccurateEyePosition(eyeLeft, 5);
        this.eyeRight=getAccurateEyePosition(eyeRight,5);

        this.SD = getStandardDeviation();
        this.rgb0 = getAverageColors();
        this.modulus0=getModulus(rgb0[0],rgb0[1],rgb0[2]);
    }

    //public get flag on face bitmap
    //
    //this function return the bitmap of flag on face
    //
    //@params: none
    //
    //@return: Bitmap of flag on face
	public Bitmap getFlagOnFace() {
    	ArrayList<int[]> edgeCoordinates = getEdgeCoordinates();
    	ArrayList<int[]> avatarCoordinates = getAvatarCoordinates(edgeCoordinates);
    	return applyFlagFilter(avatarCoordinates);
    }

    //get avatar coordinates
    //
    //this fuction concerts the smoothened edge coordinates to avatar fill coordinates
    //
    //@params: list of smoothened edge coordinates
    //
    //@return: the fill coordinates of face avatar
    ArrayList<int[]> getAvatarCoordinates(ArrayList<int[]> edgeCoordiantes){
        ArrayList<int[]> avatarCoordinates=new ArrayList<int[]>();
        //TODO: Add code to get Avatar Coordinates

        return avatarCoordinates;
    }

    Bitmap drawEdgeCoordinates(){
        ArrayList<int[]> edgeCoordinates=new ArrayList<int[]>();
        edgeCoordinates=getEdgeCoordinates();
        Bitmap flagFilter =getTransparentBitmap(200,200);
        for(int[] coordinate : edgeCoordinates) {
            flagFilter.setPixel(coordinate[0], coordinate[1], Color.argb(255,255,0,0));
        }
        return overlay(face,flagFilter);
    }
    //apply flag filter on face
    //
    //This function applys overlays the flag pixel by pixel at all the avatar coordinates
    //
    //@params: face avatar fill coordiantes
    //
    //return: a bitmap of the fliter applyed on the face
	Bitmap applyFlagFilter(ArrayList<int[]> avatarCoordinates) {
        Bitmap flagFilter =getTransparentBitmap(200,200);
		for(int[] coordinate : avatarCoordinates) {
            if(!IsInsideEye(coordinate[0],coordinate[1])) {
                flagFilter.setPixel(coordinate[0], coordinate[1], flag.getPixel(coordinate[0], coordinate[1]));
            }
		}
		return overlay(face,flagFilter);
	}

    //is a certain coordinate inside the eyes
    //
    //this function finds out if a certain coordinate is inside the eyes or not
    //
    //@params: coordinates
    //
    //@return: whether the coordinate is inside the eye or not "0 or 1"
    boolean IsInsideEye(int x,int y){
        int[] eyeOpeningRight=new int[]{
                eyeRight[0]-DEFAULT_EYE_OPENING_WIDTH,
                eyeRight[0]+DEFAULT_EYE_OPENING_WIDTH,
                eyeRight[1]-DEFAULT_EYE_OPENING_HIEGHT,
                eyeRight[1]+DEFAULT_EYE_OPENING_HIEGHT
        };
        int[] eyeOpeningLeft=new int[]{
                eyeLeft[0]-DEFAULT_EYE_OPENING_WIDTH,
                eyeLeft[0]+DEFAULT_EYE_OPENING_WIDTH,
                eyeLeft[1]-DEFAULT_EYE_OPENING_HIEGHT,
                eyeLeft[1]+DEFAULT_EYE_OPENING_HIEGHT
        };
      return (((x>eyeOpeningLeft[0]&&x<eyeOpeningLeft[1])&&(y>eyeOpeningLeft[2]&&y<eyeOpeningLeft[3]))
                        ||
                        ((x>eyeOpeningRight[0]&&x<eyeOpeningRight[1])&&(y>eyeOpeningRight[2]&&y<eyeOpeningRight[3]))
                );
    }

    //get a cropped standard image for processing
    //
    //crops the given bitmap to the standard of 200 x 200 in which this class operates
    //
    //@params: a random image
    //
    //@return: standard cropped bitmap of resolution 200 x 200
    private Bitmap getNormalizedImage(Bitmap randomImage) {
		int bitmapOrientation = randomImage.getHeight() > randomImage.getWidth()? 1 : 
				randomImage.getHeight() < randomImage.getWidth()? -1 : 0;

		switch (bitmapOrientation) {
		    case 1:
		        randomImage = Bitmap.createBitmap(randomImage, 0, (randomImage.getHeight() - randomImage.getWidth()) / 2, 
		        		randomImage.getWidth(), randomImage.getWidth());
		        break;
		    case -1:
		        randomImage = Bitmap.createBitmap(randomImage, (randomImage.getWidth() - randomImage.getHeight()) / 2, 0,
		        		randomImage.getHeight(), randomImage.getHeight());
		        break;
		}

        return Bitmap.createScaledBitmap(randomImage, 200, 200, false);
    }

    //get sharp edge coordinates
    //
    //this function finds all the rough boundary of the face
    //
    //@params : none
    //
    //@return: list of the coordinates of teh sharp boundary/edge of the face
    private ArrayList<int[]> getEdgeCoordinates() {
    	ArrayList<int[]> sharpCoordinates = new ArrayList<int[]>();

    	for (int j = 0; j < 200; j++) {
    		for (int i = 2; i < 200; i++) {
    			if(isBrighnessDifferenceRequired(i,j,i-1,j)) {
    				if(isSkinColored(i, j) && isSkinColored(i + 1, j)) {
    					sharpCoordinates.add(new int[]{i - 1, j});
    				} else if (isSkinColored(i - 1, j) && isSkinColored(i - 2, j)) {
    					sharpCoordinates.add(new int[]{i, j});
    				}
    			}
    		}
    	}
        return sharpCoordinates;
    }

    //smoothen the square edge coordinates
    //
    //TODO: @saadqureshi add description
    //
    //@params: TODO: @saadqureshi add params
    //
    //@return: returns a list of smoothened coordinates of the boundary of the face
    public ArrayList<int[]> smoothenSquarePoints(ArrayList<int[]> allEdgeCoordinates, int x, int y, int h_by_2){
        ArrayList<int[]> edgesInsideSquare = new ArrayList<int[]>(); 

        for (int j = y - h_by_2; j <= y + h_by_2; j++) {
        	for (int i = x - h_by_2; i < x + h_by_2; i++) {
        		if (allEdgeCoordinates.contains(new int[]{i, j})) {
        			edgesInsideSquare.add(new int[]{i, j});
        		}
        	}
        }

		for(int i=0;i<edgesInsideSquare.size();i++){
			int[] thisCoordiante=edgesInsideSquare.get(i);
			int[] nextCoordinate=edgesInsideSquare.get(i+1);
			allEdgeCoordinates.addAll(joinTwoPoints(thisCoordiante[0],thisCoordiante[1],nextCoordinate[0],nextCoordinate[1]));
		}
        return allEdgeCoordinates;
    }

    //join two points by a line
    //
    //joins two points by a line
    //
    //@params: x and y of point1, x and y of point two
    //
    //@return: an array of the coordinates that lie on the line joining the two points
    private ArrayList<int[]> joinTwoPoints(int x1, int y1, int x2, int y2) {
    	int greaterDiff = Math.abs(y2 - y1) > Math.abs(x2 - x1) ? y2 - y1 : x2 - x1;
    	ArrayList<int[]> linePoints = new ArrayList<int[]>(); 

    	for (int i = x1 + 1; i < x2; i += greaterDiff / (x2 - x1)) {
    		for (int j = y1 + 1; j < y2; j += greaterDiff / (y2 - y1)) {
    			linePoints.add(new int[]{i, j});
    		}
    	}
        return linePoints;
    }

    //get unit vector of the shade line
    //
    //this function finds out the unit vector from the origin to our specific rgb color
    //
    //@params: origin coordinates [0,0,0](in most cases), the average color pallete of the face
    //
    //@return: the unit vector from the origin to the average color
    private int[] getLineUnitVector(int[] origin, int[] averageColor) {
    	int red = averageColor[0] - origin[0], green = averageColor[1] - origin[1], blue = averageColor[2] - origin[2];
    	int magnitude = (int) Math.sqrt(Math.pow(red, 2) + Math.pow(green, 2) + Math.pow(blue, 2));

    	return new int[]{red / magnitude, green / magnitude, blue / magnitude};
    }

    //@saadqureshi : NOTE:: I have changed this function. Now it uses the cone formula
    //
    //is a pixel skin colored
    //
    // finds out if the color of a pixel lies inside the cone extending from the origin and has a radius equal to
    // Standard deviation at the average pallete color but limited at top and bottom brightness limits.
    //
    //@params: coordinates of the you want to know is skin colored
    //
    //@returns: whether that pixel is skin colored or not
    private boolean isSkinColored(int x, int y) {


        int[] rgbx=new int[]{ Color.red(face.getPixel(x, y)) ,
                Color.green(face.getPixel(x, y)) , Color.blue(face.getPixel(x, y))};

        double modulusX=getModulus(rgbx[0],rgbx[1],rgbx[2]);
        double dotProductRGB0andRGBX = rgb0[0]*rgbx[0] + rgb0[1]*rgbx[1] + rgb0[2]*rgbx[2];

		//return (red / abc[0] == green / abc[1]) && (red / abc[0] == blue / abc[2]);

        Log.v("FLAGOVERLAY", "Determining if isSkinColored");
        // EQUATION OF CONE
        return (
        /*This should be*/                             ((SD * modulusX)/modulus0)
        /* GREATER THAN */                                         >
        /*     this     */ (((Math.pow(modulus0,2)*Math.pow(modulusX,2))-Math.pow(dotProductRGB0andRGBX,2))/Math.pow(modulus0,2))
                                                                  &&
                    (modulusX>DEFAULT_BOTTOM_BRIGHTNESS_LIMIT_OF_SKIN_COLOR&&modulusX<DEFAULT_TOP_BRIGHTNESS_LIMIT_OF_SKIN_COLOR)

        );
    }

    //find the modulus of an rgb value
    //
    //finds the modulus i.e. distance of a certain rgb value from the origin
    //
    //@params: the r g b values of the color under consideration
    //
    //@return: the modulus
    private double getModulus(int r, int g, int b){
        return Math.sqrt(Math.pow(r,2)+Math.pow(g,2)+Math.pow(b,2));
    }

    private boolean isBrighnessDifferenceRequired(int x1, int y1, int x2, int y2) {
    	int[] rgb1 = new int[]{Color.red(face.getPixel(x1, y1)), Color.green(face.getPixel(x1, y1)),
				Color.blue(face.getPixel(x1, y1))};
		int[] rgb2 = new int[]{Color.red(face.getPixel(x2, y2)), Color.green(face.getPixel(x2, y2)),
				Color.blue(face.getPixel(x2, y2))};

    	return (Math.sqrt(Math.pow(Math.abs(rgb2[0] - rgb1[0]), 2) + Math.pow(Math.abs(rgb2[1] - rgb1[1]) , 2)
    				+ Math.pow(Math.abs(rgb2[2] - rgb1[2]) , 2))) > DEFAULT_GRADIENT_DECENT;
    }

    //get the average of the colors in the cheek of the face
    //
    //Averages all the reds, greens and blues of the colores in the cheeks of the face image
    //
    //@params: none
    //
    //@return: an array containing the { avgR , avgG , avgB } average rgb values
    public int[] getAverageColors() {
        ArrayList<Integer> allReds = new ArrayList<>();
        ArrayList<Integer> allGreens = new ArrayList<>();
        ArrayList<Integer> allBlues = new ArrayList<>();


        for(int i = (int)(cheekLeft[0] - cheekWidthByTwo); i <= cheekLeft[0] + cheekWidthByTwo; i++) {
            for(int j = (int)(cheekLeft[1] - cheekHeightByTwo); j <= cheekLeft[1] + cheekHeightByTwo; j++) {
                allReds.add(Color.red(face.getPixel(i, j)));
                allGreens.add(Color.green(face.getPixel(i, j)));
                allBlues.add(Color.blue(face.getPixel(i, j)));
            }
        }

        for(int i = (int)(cheekRight[0] - cheekWidthByTwo); i <= cheekRight[0] + cheekWidthByTwo; i++) {
            for(int j = (int)(cheekRight[1] - cheekHeightByTwo); j <= cheekRight[1] + cheekHeightByTwo; j++) {
                allReds.add(Color.red(face.getPixel(i, j)));
                allGreens.add(Color.green(face.getPixel(i, j)));
                allBlues.add(Color.blue(face.getPixel(i, j)));
            }
        }

        return new int[]{getAverage(allReds), getAverage(allGreens), getAverage(allBlues)};
    }

    //get the standard deviation of the rgb values in the cheeks
    //
    //find of the standard deviation in the rgb values of the cheeks by taking the modulus of standard deviations in the reds, greens and blues
    //
    //@params: none
    //
    //@return: standard deviation in the rgb values of the cheeks
    public double getStandardDeviation(){
        ArrayList<Integer> allReds = new ArrayList<>();
        ArrayList<Integer> allGreens = new ArrayList<>();
        ArrayList<Integer> allBlues = new ArrayList<>();

        for(int i = (int)(cheekLeft[0] - cheekWidthByTwo); i <= cheekLeft[0] + cheekWidthByTwo; i++) {
            for(int j = (int)(cheekLeft[1] - cheekHeightByTwo); j <= cheekLeft[1] + cheekHeightByTwo; j++) {
                allReds.add(Color.red(face.getPixel(i, j)));
                allGreens.add(Color.green(face.getPixel(i, j)));
                allBlues.add(Color.blue(face.getPixel(i, j)));
            }
        }

        for(int i = (int)(cheekRight[0] - cheekWidthByTwo); i <= cheekRight[0] + cheekWidthByTwo; i++) {
            for(int j = (int)(cheekRight[1] - cheekHeightByTwo); j <= cheekRight[1] + cheekHeightByTwo; j++) {
                allReds.add(Color.red(face.getPixel(i, j)));
                allGreens.add(Color.green(face.getPixel(i, j)));
                allBlues.add(Color.blue(face.getPixel(i, j)));
            }
        }

        return Math.sqrt(Math.pow(getStnDev(allReds),2)+Math.pow(getStnDev(allGreens),2)+Math.pow(getStnDev(allBlues),2));
    }

    //get average of list
    //
    //get the average of an ArrayList of integers
    //
    //@params: ArrayList of integers
    //
    //@return: average of the ArrayList of integers
    private int getAverage(ArrayList<Integer> array) {
        int sum = 0;

        for(Integer number : array) {
            sum += number;
        }

        return sum / array.size();
    }

    //get Accurate eye position in the face
    //
    //converts the rough location of the eyes from the google api into exact coordinates of the eyeballs
    //
    //@params: rough coordinates of the eye, radius from those coordinates that includes the complete eyeball but excludes anything other than skin
    //
    //@return: the exact coordinates of the eyeballs
	public int[] getAccurateEyePosition(int[] EyePosition, int radius){
		double sumxs=0;
		double sumys=0;
		int xs=0;
		int ys=0;
		int h=EyePosition[0];
		int k=EyePosition[1];
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                if (isInsideEyeCircle(new int[]{i, j}, h, k, radius) && !isSkinColored(i, j)){
                    sumxs+=i;
                    xs++;
                    sumys+=j;
                    ys++;
                }
            }
        }


        return new int[]{(int) (sumxs/xs), (int) (sumys/ys)};
	}

    //are the coordinates inside the eye circle defined earlier( ^ refer to documentaion of above function )
    //
    //finds out if the current coordinates lie inside the circle that include nothing but eyeball and skin
    //
    //@params: current coordinate under consideration, the coordinates of the eyes as given by google api
    //
    //@return: is the current coordinate inside the circle around the eye
	public boolean isInsideEyeCircle(int[] coordinates,int h,int k,int radius){
		return (
				radius>(Math.sqrt(Math.pow(coordinates[0]-h,2)+Math.pow(coordinates[1]-k,2)))
		);
	}

    //overlay bmap2 on bmp1
    //
    //sets alpha (transparency) of bmp2 (flag filter) and overlays it on bmp1 (face) 
    //
    //@params: two bitmaps of face and flag filter
    //
    //@return: a bitmap of flagfilter overlayed on face with an alpha of 140
	private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
		Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
		Canvas canvas = new Canvas(bmOverlay);
		Paint paint = new Paint();
		paint.setAlpha(140);
		canvas.drawBitmap(bmp1, new Matrix(), null);
		canvas.drawBitmap(bmp2, new Matrix(), paint);
		return bmOverlay;
	}

    //get a transparent empty bitmap
    //
    //returns an empty transparent bitmap that can be used as a canvas for the flag filter
    //
    //@params: the resolution of the tranparent bitmap needed
    //
    //@return: transparent bitmap of said resolution
	private Bitmap getTransparentBitmap(int xRes,int yRes){
		Bitmap bmOverlay = Bitmap.createBitmap(xRes, yRes, null);
		Canvas canvas = new Canvas(bmOverlay);
		Paint paint = new Paint();
		paint.setAlpha(0);
		canvas.drawBitmap(bmOverlay,new Matrix(),paint);
		return  bmOverlay;
	}

    //get the standard deviation of a primitive color r g or b
    //
    //finds out the standard deviation from an ArrayList of Integers ( values of a primitive color { r g or b})
    //
    //@params: list of reds, greens or blues
    //
    //@return: standard deviation in the list of values of a primitive color
    private double getStnDev(ArrayList<Integer> array) {
        double average = getAverage(array);
        double sum = 0;
        double variance;

        for(Integer number : array) {
            sum += Math.pow(Math.abs(number - average), 2);
        }

        variance = sum / (double) array.size();

        return Math.sqrt(variance);
    }
}