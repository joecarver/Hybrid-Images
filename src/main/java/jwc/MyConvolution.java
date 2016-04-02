package jwc;

import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.image.*;
import org.openimaj.image.processing.convolution.FFastGaussianConvolve;

public class MyConvolution implements SinglebandImageProcessor<Float, FImage> {

	//kernel can be arbitrary size but both dimensions must be odd
	private float[][] kernel;
	
	public MyConvolution(float cutoff, float[][] kernel) {
		this.kernel = kernel;
	}
	
	@Override
	/* convolve image with kernel and store result back in image
	 * 
	 * Adapted from Feature Extraction & Image Processing - Nixon, Aguado (Code Snippet 3.5)
	 */
	public void processImage(FImage image)
	{
		int imgRows = image.height;
		int imgCols = image.width;
		
		int tempRows = kernel.length;
		int tempCols = kernel[0].length;
		
		int tempROWS_half = (int) Math.floor(tempRows/2);
		int tempCOLS_half = (int) Math.floor(tempCols/2);
		
		//set a temporary image to black
		FImage temporary  = new FImage(imgCols, imgRows);
		temporary.fill(0f);
		
		
		for(int x=tempROWS_half+1; x<imgCols-tempROWS_half; x++){ //address all columns except border
			for(int y=tempCOLS_half+1; y<imgRows-tempCOLS_half; y++){ //address all rows except border
				float sum = 0;
				for(int iWin=1; iWin<tempRows; iWin++){ //address template rows
					for(int jWin=1; jWin<tempCols; jWin++){ //address template columns
						sum = sum + image.getPixel(x+iWin-tempROWS_half-1, y+jWin-tempCOLS_half-1) * kernel[jWin][iWin];
					}
				}
				temporary.setPixel(x, y, sum);	
			}	
		}
		
		//Put all pixels within range 0.0-1.0
		temporary = temporary.normalise();
		
		//modify passed image
		image.internalAssign(temporary);
	}
}
