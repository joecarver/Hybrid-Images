package jwc;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.convolution.Gaussian2D;
import org.openimaj.image.processing.resize.ResizeProcessor;

public class HybridProcessor 
{	
	private int cutoff1, cutoff2;
	private int size;
	
	MBFImage image1, image2;
	MBFImage LPimage1, LPimage2;
	MBFImage HPimage1, HPimage2;
	MBFImage hybrid1, hybrid2;
	MBFImage currentHybrid;

	/*
	 * Returns a 2-element array containing both possible combinations of hybrid image
	 *  
	 *  getHybridImages()[0] = image1.highPass() + image2.lowPass()
	 *  getHybridImages()[1] = image2.highPass() + image1.lowPass()
	 *  
	 */
	public MBFImage[] getHybridImages(int cutoff1, int cutoff2)
	{
		//read user specified value for sigma
		this.cutoff1 = cutoff1;
		this.cutoff2 = cutoff2;
		
		//set size to function of sigma:
    	size = (int) (8.0f * cutoff1 + 1.0f); // (this implies the window is +/- 4 sigmas from the centre of the Gaussian)
    	if(size % 2 == 0) size++; //ensure size is odd
    	
    	//create gaussian images to be used as template for convolution
    	FImage gauss1 = Gaussian2D.createKernelImage(size, cutoff1);
    	MyConvolution conv1 = new MyConvolution(cutoff1, gauss1.pixels);
    	
    	FImage gauss2 = Gaussian2D.createKernelImage(size, cutoff2);
    	MyConvolution conv2 = new MyConvolution(cutoff2, gauss2.pixels);
    	
    	//LP - convolve image with guassian filter
    	LPimage1 = image1.process(conv1);
    	LPimage2 = image2.process(conv2);
    	
    	//HP - subtract a low pass version of an image from itself
    	HPimage1 = image1.subtract(LPimage1);
    	HPimage2 = image2.subtract(LPimage2);

    	//create both versions of hybrid
    	hybrid1 = HPimage1.add(LPimage2);
    	hybrid2 = HPimage2.add(LPimage1);
    	
    	//array to return both images
		MBFImage[] hybrids = new MBFImage[2];
		hybrids[0] = hybrid1;	hybrids[1] = hybrid2;
	
		return hybrids;
	}
	
	/*
	 * Return an MBFImage containing Low Pass and High Pass versions of each image
	 */
	public MBFImage displayFilteredImages()
	{
		MBFImage filteredImages = new MBFImage(image1.getWidth()*2, image1.getHeight()*2);
		
		//Actual HPimage has negative values - zero-mean averaging 
		//so to visualise, add 0.5 to each pixel of each band 
		MBFImage HPimgVis1 = new MBFImage((HPimage1.getBand(0).add(0.5f)), (HPimage1.getBand(1).add(0.5f)), HPimage1.getBand(2).add(0.5f));
		MBFImage HPimgVis2 = new MBFImage((HPimage2.getBand(0).add(0.5f)), (HPimage2.getBand(1).add(0.5f)), HPimage2.getBand(2).add(0.5f));
		
		filteredImages.drawImage(HPimgVis1, 0, 0);
		filteredImages.drawImage(HPimgVis2, image1.getWidth(), 0);
		filteredImages.drawImage(LPimage1, 0, image1.getHeight());
		filteredImages.drawImage(LPimage2, image1.getWidth(), image1.getHeight());
		
		return filteredImages;		
	}
	
	public MBFImage getScaledImages(){
		
		ArrayList<MBFImage> resized = new ArrayList<MBFImage>();
		
		//Create copy of image for resizing, add to array
		MBFImage tmp = currentHybrid;
		resized.add(tmp);
		
		//add 5 smaller images to array
		for(int i=0; i<6; i++){
			resized.add(tmp=ResizeProcessor.halfSize(tmp));
		}
		
		//Calculate total height + width of adjacent scaled images
		int totalHeight = currentHybrid.getHeight(); 
		int totalWidth = 0;
		for(MBFImage m : resized){
			totalWidth += m.getWidth();
		}
		
		//Create an image on which to draw the scaled images
		MBFImage scaledAll = new MBFImage(totalWidth, totalHeight);
	
		for(MBFImage m : resized)
		{
				int imgStart_y = totalHeight-m.getHeight();
				int imgStart_x = 0;
				
				//image start point (horizontal) = sum of widths of all preceeding images
				for(MBFImage sml : resized){
					if(resized.indexOf(sml) < resized.indexOf(m)){
						imgStart_x += sml.getWidth();
					}
				}
				
			scaledAll.drawImage(m, imgStart_x, imgStart_y);	
		}
	
		return scaledAll;
	}
	
	//getters and setters for images
	public MBFImage getImage1(){
		return image1;
	}
	
	public void setImage1(MBFImage i){
		this.image1 = i;
	}
	
	public MBFImage getImage2(){
		return image2;
	}
	
	public void setImage2(MBFImage i){
		this.image2 = i;
	}
	
	public void setCurrentHybrid(MBFImage i){
		this.currentHybrid = i;
	}
	
	public MBFImage getHybrid1(){
		return this.hybrid1;
	}
	
	public MBFImage getCurrentHybrid() {
		return this.currentHybrid;
	}

}

