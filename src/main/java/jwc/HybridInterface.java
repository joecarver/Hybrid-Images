package jwc;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.convolution.FFastGaussianConvolve;
import org.openimaj.image.processing.convolution.Gaussian2D;
import org.openimaj.image.processing.resize.ResizeProcessor;

public class HybridInterface extends JPanel {

	//JFrame that holds this panel
	JFrame container;	
	
	//pop-up to hold hybrid image
	JFrame resultFrame;
	JFrame resultFrame_alt;
	JFrame resizedFrame;
	JFrame filteredFrame;
	
	//Values for JSliders
	final static int MIN_CUTOFF = 0;
	final static int DEFAULT_CUTOFF = 4;
	final static int DEFAULT_CUTOFF_2 = 8;
	final static int MAX_CUTOFF = 10;
	
	//basic interface elements
	JButton img1choose = new JButton("Choose first Image");
	JButton img2choose = new JButton("Choose second Image");
	JLabel img1deets = new JLabel("Image 1: ");
	JLabel img2deets = new JLabel("Image 2: "); 
	
	//User-controllable sliders to adjust sigma
	JSlider cutoffSlider1 = new JSlider(MIN_CUTOFF, MAX_CUTOFF, DEFAULT_CUTOFF);
	JLabel cutoffLabel1 = new JLabel("IMAGE 1 Cut-off/Sigma: " + DEFAULT_CUTOFF);
	JSlider cutoffSlider2 = new JSlider(MIN_CUTOFF, MAX_CUTOFF, DEFAULT_CUTOFF_2);
	JLabel cutoffLabel2 = new JLabel("IMAGE 2 Cut-off/Sigma: " + DEFAULT_CUTOFF_2);
	
	//Buttons to generate hybrid images, various sizes, or stages in pipeline
	JButton hybridCreate = new JButton("Combine!");
	JButton hybridOther = new JButton("Combine (alt)");
	JButton displayFiltered = new JButton("Display LP/HP images");
	JButton displayScaled = new JButton("Display Scaled Images");
	
	//left for detail panels, right for image panels
	JPanel leftPanel = new JPanel();
	JPanel rightPanel = new JPanel();
	
	//To occupy left side of interface
	JPanel img1SelectPanel = new JPanel(new FlowLayout());
	JPanel img1CutoffPanel = new JPanel(new FlowLayout());
	JPanel img2SelectPanel = new JPanel(new FlowLayout());
	JPanel img2CutoffPanel = new JPanel(new FlowLayout());
	JPanel createHybridPanel = new JPanel(new FlowLayout());
	JPanel extrasPanel = new JPanel(new FlowLayout());
	
	//JPanels hold the ImageComponent instances, which can be assigned a picture
	JPanel imgPanel1 = new JPanel();
	JPanel imgPanel2 = new JPanel();
	ImageComponent imgComp1, imgComp2;
	
	HybridProcessor model;
	ResizeProcessor resizer;
	

	//constructor sets initial dimensions and populates frame with all panels
	public HybridInterface(JFrame container, HybridProcessor model)
	{
		//model handles all image processing
		this.model = model;
		
		//the enclosing JFrame - needed for dimensions
		this.container = container;
		
		//initialize frames to appear on button clicks
		resultFrame = DisplayUtilities.createNamedWindow("Hybrid", "Hybrid", true);
		resultFrame_alt = DisplayUtilities.createNamedWindow("Inverse", "Inverse Hybrid", true);
		resizedFrame = DisplayUtilities.createNamedWindow("Scaled", "Scaled...", true);
		filteredFrame = DisplayUtilities.createNamedWindow("HPLP", "High-Pass & Low-Pass Filtered Images", true);
		
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(700, 500));
		
		//set LHS & RHS panels to having vertically ordered layout
		leftPanel.setLayout(new BoxLayout(leftPanel,BoxLayout.PAGE_AXIS));
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
		
		//set-up panels & components to hold preview images
		imgComp1 = new DisplayUtilities.ImageComponent(true, false);
		imgComp2 = new DisplayUtilities.ImageComponent(true, false);
		imgPanel1.add(imgComp1);
		imgPanel2.add(imgComp2);
		
		//add buttons/sliders to all the LHS panels
		img1SelectPanel.add(img1choose); img1SelectPanel.add(img1deets);
		img2SelectPanel.add(img2choose); img2SelectPanel.add(img2deets);
		img1CutoffPanel.add(cutoffSlider1); img1CutoffPanel.add(cutoffLabel1);
		img2CutoffPanel.add(cutoffSlider2); img2CutoffPanel.add(cutoffLabel2);
		createHybridPanel.add(hybridCreate); createHybridPanel.add(hybridOther);
		extrasPanel.add(displayFiltered); extrasPanel.add(displayScaled);
		
		//add control panels to LHS
		leftPanel.add(img1SelectPanel); leftPanel.add(img1CutoffPanel); 
		leftPanel.add(img2SelectPanel); leftPanel.add(img2CutoffPanel);
		leftPanel.add(createHybridPanel); leftPanel.add(extrasPanel);
		
		//add image panels to RHS
		rightPanel.add(imgPanel1);
		rightPanel.add(imgPanel2);
		
		//add LHS & RHS to main
		this.add(leftPanel, BorderLayout.LINE_START);
		this.add(rightPanel, BorderLayout.LINE_END);
		
	}

	//waits for listener events
	public void init()
	{
		//File chooser interface for first image
		img1choose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser jf = new JFileChooser("data");
				int returnValue = jf.showOpenDialog(null);
				
				if(returnValue == JFileChooser.APPROVE_OPTION)
				{
					File chosen = jf.getSelectedFile();
					try {
						model.setImage1(ImageUtilities.readMBF(chosen));
					} 
					catch (IOException e1) {
						e1.printStackTrace();	
					}
					
					img1deets.setText("Image 1 : " +chosen.getName());
					
					//get window dimensions and pass a ratio to a ResizeProcessor
					//this is to show a preview that fits in the main interface window
					int height, width;
					height = container.getHeight();
					width = container.getWidth();
					resizer = new ResizeProcessor((int)(width*0.4),(int)(height/2.5), true);
					
					//get current image1, resize it according to window height, convert to buffered image and display
					imgComp1.setImage((ImageUtilities.createBufferedImage(model.getImage1().process(resizer))));
					
					
				}
			}	
		});
		
		//File chooser for 2nd image
		img2choose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser jf = new JFileChooser("data");
				int returnValue = jf.showOpenDialog(null);
				
				if(returnValue == JFileChooser.APPROVE_OPTION)
				{
					File chosen = jf.getSelectedFile();
					try {
						model.setImage2(ImageUtilities.readMBF(chosen));
					} 
					catch (IOException e1){
						e1.printStackTrace();
					}
					
					img2deets.setText("Image 2: " + chosen.getName());
					
					int height, width;
					height = container.getHeight();
					width = container.getWidth();
					resizer = new ResizeProcessor((int)(width*0.4),(int)(height/2.5), true);
					//get current image1, resize it according to window height, convert to buffered image and display
					imgComp2.setImage(ImageUtilities.createBufferedImage(model.getImage2().process(resizer)));
				}
			}	
		});
	
		//Create both hybrids and access the 1st in the array, set this globally in model as currentHybrid, then display
		hybridCreate.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{	
				MBFImage hybrid1 = model.getHybridImages(cutoffSlider1.getValue(), cutoffSlider2.getValue())[0];
				model.setCurrentHybrid(hybrid1);
				DisplayUtilities.display(model.getCurrentHybrid(), resultFrame);
			}
		});
		
		//Create both hybrids and access the 2st in the array, set this globally in model as currentHybrid, then display
		hybridOther.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				MBFImage hybrid2 = model.getHybridImages(cutoffSlider1.getValue(), cutoffSlider2.getValue())[1];
				model.setCurrentHybrid(hybrid2);
				DisplayUtilities.display(model.getCurrentHybrid(), resultFrame_alt);
			}
		});
		
		displayFiltered.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				//Update hybrid with latest images/cutoff values
				model.getHybridImages(cutoffSlider1.getValue(), cutoffSlider2.getValue());
				model.setCurrentHybrid(model.getHybrid1());
				
				MBFImage filtered = model.displayFilteredImages();
				DisplayUtilities.display(filtered, filteredFrame);
			}
		});
		
		displayScaled.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e)
			{
				model.getHybridImages(cutoffSlider1.getValue(), cutoffSlider2.getValue());
				model.setCurrentHybrid(model.getHybrid1());
				
				MBFImage scaled = model.getScaledImages();
				DisplayUtilities.display(scaled, resizedFrame);	
			}
		});
		
		//Listener updates label to show actual value of cutoff
		cutoffSlider1.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				cutoffLabel1.setText("IMAGE 1 Cut-off/Sigma: " + cutoffSlider1.getValue());
			}
		});
		
		cutoffSlider2.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				cutoffLabel2.setText("IMAGE 2 Cut-off/Sigma: " + cutoffSlider2.getValue());
			}
		});
		
	}
}
