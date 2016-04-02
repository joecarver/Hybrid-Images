package jwc;
import javax.swing.JFrame;

public class MainFrame extends JFrame {

	public MainFrame(String title)
	{
		super(title);
	}

	void init()
	{
		HybridProcessor hp = new HybridProcessor();
		
		HybridInterface options = new HybridInterface(this, hp);
		pack();
		options.init();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(options);
		setVisible(true);
		pack();
	}
	
}