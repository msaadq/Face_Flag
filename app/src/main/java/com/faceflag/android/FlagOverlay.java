package com.faceflag.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.util.ArrayList;

class FlagOverlay {

	static private final int DEFAULT_CHEECK_RANGE=2;

	static private int DEFAULT_GRADIENT_DECENT = 10;
	static private int incrementalFactorX=6;
	static private int incrementalFactorY=1;


    static final private int DEFAULT_EYE_OPENING_WIDTH=7;
    static final private int DEFAULT_EYE_OPENING_HIEGHT=2;
	
	private Bitmap face;
	private Bitmap flag;

	private int[] cheekLeft;
	private int[] cheekRight;

    private int[] eyeLeft;
    private int[] eyeRight;

	public FlagOverlay( Bitmap face, Bitmap flag, int[] cheekLeft, int[] cheekRight,int[] eyeLeft,int[] eyeRight) {
		this.face = getNormalizedImage(face);
		this.flag = getNormalizedImage(flag);

		this.cheekLeft = cheekLeft;
		this.cheekRight = cheekRight;

        this.eyeLeft=getAccurateEyePosition(eyeLeft,5);
        this.eyeRight=getAccurateEyePosition(eyeRight,5);
	}

	public Bitmap getFlagOnFace() {
    	ArrayList<int[]> edgeCoordinates = getEdgeCoordinates();
    	ArrayList<int[]> avatarCoordinates = getAvatarCoordinates(edgeCoordinates);
    	return applyFlagFilter(avatarCoordinates);
    }

    ArrayList<int[]> getAvatarCoordinates(ArrayList<int[]> edgeCoordiantes){
        ArrayList<int[]> avatarCoordinates=new ArrayList<int[]>();
        //TODO: Add code to get Avatar Coordinates
        return avatarCoordinates;
    }
	Bitmap applyFlagFilter(ArrayList<int[]> avatarCoordinates) {
        Bitmap flagFilter =getTransparentBitmap(200,200);
		for(int[] coordinate : avatarCoordinates) {
            if(!IsInsideEye(coordinate[0],coordinate[1])) {
                flagFilter.setPixel(coordinate[0], coordinate[1], flag.getPixel(coordinate[0], coordinate[1]));
            }
		}
		return overlay(face,flagFilter);
	}

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

    private int[] getLineUnitVector(int[] origin, int[] averageColor) {
    	int red = averageColor[0] - origin[0], green = averageColor[1] - origin[1], blue = averageColor[2] - origin[2];
    	int magnitude = (int) Math.sqrt(Math.pow(red, 2) + Math.pow(green, 2) + Math.pow(blue, 2));

    	return new int[]{red / magnitude, green / magnitude, blue / magnitude};
    }

    private boolean isSkinColored(int x, int y) {
   		int red = Color.red(face.getPixel(x, y));
   		int green = Color.green(face.getPixel(x, y));
   		int blue = Color.blue(face.getPixel(x, y));
   		int[] abc = getLineUnitVector(new int[]{0, 0, 0}, getAverageColors());
		
		return (red / abc[0] == green / abc[1]) && (red / abc[0] == blue / abc[2]);
    }

    private boolean isBrighnessDifferenceRequired(int x1, int y1, int x2, int y2) {
    	int[] rgb1 = new int[]{Color.red(face.getPixel(x1, y1)), Color.green(face.getPixel(x1, y1)),
				Color.blue(face.getPixel(x1, y1))};
		int[] rgb2 = new int[]{Color.red(face.getPixel(x2, y2)), Color.green(face.getPixel(x2, y2)),
				Color.blue(face.getPixel(x2, y2))};

    	return (Math.sqrt(Math.pow(Math.abs(rgb2[0] - rgb1[0]), 2) + Math.pow(Math.abs(rgb2[1] - rgb1[1]) , 2)
    				+ Math.pow(Math.abs(rgb2[2] - rgb1[2]) , 2))) > DEFAULT_GRADIENT_DECENT;
    }

    public int[] getAverageColors() {
        ArrayList<Integer> allReds = new ArrayList<>();
        ArrayList<Integer> allGreens = new ArrayList<>();
        ArrayList<Integer> allBlues = new ArrayList<>();

        for(int i = (int)(cheekLeft[0] - DEFAULT_CHEECK_RANGE * incrementalFactorX); i <= cheekLeft[0] + DEFAULT_CHEECK_RANGE * incrementalFactorX; i++) {
            for(int j = (int)(cheekLeft[1] - DEFAULT_CHEECK_RANGE * incrementalFactorY); j <= cheekLeft[1] + DEFAULT_CHEECK_RANGE * incrementalFactorY; j++) {
                allReds.add(Color.red(face.getPixel(i, j)));
                allGreens.add(Color.green(face.getPixel(i, j)));
                allBlues.add(Color.blue(face.getPixel(i, j)));
            }
        }

        for(int i = (int)(cheekRight[0] - DEFAULT_CHEECK_RANGE * incrementalFactorX); i <= cheekRight[0] + DEFAULT_CHEECK_RANGE * incrementalFactorX; i++) {
            for(int j = (int)(cheekRight[1] - DEFAULT_CHEECK_RANGE * incrementalFactorY); j <= cheekRight[1] + DEFAULT_CHEECK_RANGE * incrementalFactorY; j++) {
                allReds.add(Color.red(face.getPixel(i, j)));
                allGreens.add(Color.green(face.getPixel(i, j)));
                allBlues.add(Color.blue(face.getPixel(i, j)));
            }
        }

        return new int[]{getAverage(allReds), getAverage(allGreens), getAverage(allBlues)};
    }


    private int getAverage(ArrayList<Integer> array) {
        int sum = 0;

        for(Integer number : array) {
            sum += number;
        }

        return sum / array.size();
    }




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
	public boolean isInsideEyeCircle(int[] coordinates,int h,int k,int radius){
		return (
				radius>(Math.sqrt(Math.pow(coordinates[0]-h,2)+Math.pow(coordinates[1]-k,2)))
		);
	}
	private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
		Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
		Canvas canvas = new Canvas(bmOverlay);
		Paint paint = new Paint();
		paint.setAlpha(140);
		canvas.drawBitmap(bmp1, new Matrix(), null);
		canvas.drawBitmap(bmp2, new Matrix(), paint);
		return bmOverlay;
	}

	private Bitmap getTransparentBitmap(int xRes,int yRes){
		Bitmap bmOverlay = Bitmap.createBitmap(xRes, yRes, null);
		Canvas canvas = new Canvas(bmOverlay);
		Paint paint = new Paint();
		paint.setAlpha(0);
		canvas.drawBitmap(bmOverlay,new Matrix(),paint);
		return  bmOverlay;
	}

}