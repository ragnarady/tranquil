package com.ragnarady.puyogame;

import java.util.Random;
import javax.swing.*;	//JFrame,JComponent,Timer
import java.awt.*;		//Dimension,Image,Toolkit,Graphics,Container,Color,Graphics2D
						//RenderingHints,GradientPaint,Font,Rectangle,AlphaComposite
import java.awt.event.*;//ActionEvent,ActionListener,KeyAdapter,KeyEvent

public class Game extends JFrame {
	GamePane gp;			//GamePane is subclass of JComponent on which the puyos are moved		
	int width,height;
	int rows,columns;		
	int nodeSize;			//holds the length of the each puyo(because it is a square piece)
	Dimension screenSize;	//holds the dimension of the screen in terms of resolution
	public Game() 
	{
		super("PuyoGame");
		columns = 6;			// Can be set to any value, game rows*columns depends on this value	
		rows = 12;
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		width = screenSize.width;
		height = screenSize.height;
		//Width and Height holds the screen resolution.
		//Different computers may have set with different resolutions.
		//If the Window size is static then it is differ to see from one computer to another.
		//To place the window at the middle of the screen at any resolution and 
		//to adjust the window size and puyo size some calculations are taken here.
		//Normally resolution(for pc) is windth*height format with 8:6 ratio.
		//For puyo game(rows=12,colums=6) window take 1:2(wd*ht) ratio in screen resolution.
		//So in 8 parts of width(of screen) 2 parts is assigned to window widht and
		//and in 6 parts of height(of screen) 4 parts is assigned to window height to place
		//window at the middle of the screen.
		//for puyo(cell size) width and height are same
		//puyo size for 12 rows 6 colums puyo game
		nodeSize = (width/8)*2/columns;			//or (height/6)*4/12 
		gp = new GamePane(nodeSize,rows,columns);
		Container c = getContentPane();
		c.add(gp);
		setResizable(false);
		//placing window(added score board size) at the middle of the screen
		//3 puyos width is added to the window for score board display
		setBounds ((width/8)*3-nodeSize*3/2,(height/6)*1-nodeSize,(width/8)*2+nodeSize*3+6,(height/6)*4+25+nodeSize);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	public static void main(String args[]) {
		System.out.println("PuyoGame is running now");
		new Game();
	}
}
class GamePane extends JComponent implements ActionListener
{
	static int rows,columns;	
	static int scr[][];			//scr or(screen) array holds the information about puyos to display
	Node tetris;				//Formation of Tetris is checked using this object
	Timer timer,timer1,timer2,anim_timer;	//different timers used for animation of puyos
	Image img[]=new Image[4];	//holds 4 puyo images
	Toolkit tk;			//used to load the images		
	Random rand;		//this object used to generate puyos randomly in color.
	int rot;			//used for the rotation of the puyos
	int len;			//length of the puyo ie. width and height
	boolean isPuyoDropped;	//generated puyo isPuyoDropped the bottom or in movement
	int count;			//count of puyos when formed tetris to delete
	boolean isGameStarted;	//Game is started or not
	boolean isGameLost,isGamePaused; 
	int leftPuyo,rightPuyo;			//The two puyos generate to be next are stored in leftPuyo and rightPuyo
	int level,score,pieces,puyosRemoved;//pieces is number of joint puyos(single piece) generated
					//number of removed puyos by forming tetris
	int minscore;	
	int anim;		//to build the pixel by pixel animation (movement of generated puyos)
	float alpha,alpha1;
	boolean levelflag;
	public GamePane(int l,int r,int c)
	{
		len = l;		//length of puyos is set by the PuyoGame class where it is calculated and sent here
		rows = r+1;	//number of rows is taken 12+1 for the easyness of generating puyos from pipe.
					//extra one row is occupied by pipe at the top. Only 12 rows are used for puyos.
		columns = c;	    //6 columns
		init();		//initializing game data
		//puyo images are selected depending on the length of puyos
		//length of puyo is calculated depending on the resolution of screen
		//In this game i used two types of puyos for 
		//(i)800*600 and below resolutions
		//(ii)1024*768 and above resolutions
		//So images are loaded after length of puyos is calculated
		loadImages();
		generatePuyos();				//to start the generating puyos
		addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if(e.getKeyCode()==KeyEvent.VK_ENTER)
				{
					//if game is not isGameStarted then start the game by starting timer when enter is pressed
					//ig game is over then initialize the variables and start generating puyos also
					if(!isGameStarted)		
					{
						setDelays();
						timer.start();
						isGameStarted = true;
					}
					if(isGameLost)		
					{
						init();
						generatePuyos();
						isGameStarted = false;
					}
					if(isGamePaused)
					{
						init();
						generatePuyos();
						isGameStarted = false;
					}
					repaint();
				}
				else
				if(e.getKeyCode() == KeyEvent.VK_LEFT && !isPuyoDropped && !isGamePaused)//move puyos left if each puyo not isPuyoDropped to ground
				{
					moveLeft();
				}
				else
				if(e.getKeyCode() == KeyEvent.VK_RIGHT && !isPuyoDropped && !isGamePaused)
				{
					moveRight();
				}
				else
				if(e.getKeyCode() == KeyEvent.VK_UP && !isGamePaused )
				{
					if(!isPuyoDropped)
					rotate();
					if(!isGameStarted && level<19)	//Before strting the game 
						level++;
				}
				else
				if(e.getKeyCode() == KeyEvent.VK_DOWN && !isGamePaused)
				{
					moveDown();
					if(!isGameStarted && level>0) 	//Before strting the game 
					level--;
				}
				else
				if(e.getKeyCode() == KeyEvent.VK_P && isGameStarted && !isGameLost)
				{
					//if game is already paused then resume it, other wise pause the game
					if(isGamePaused)
					{
						isGamePaused = false;
						alpha1 = 0.0f;
						timer.start();		//game is resumed
					}
					else
					{
						timer.stop();
						isGamePaused = true;		//game is paused
					}
				}
				else
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					//if game is already paused then exit the game, other wise pause the game
					if( isGameStarted && !isGameLost)
					{
						if(isGamePaused)
						System.exit(0);		//exit the game
						else
						{
							timer.stop();
							isGamePaused = true;		//game is paused
						}
					}
					else
					System.exit(0);
				}
			}
		});
		setFocusable(true);			//to set the keyboard focus on this game pane
	}
	public void init()//all variables declared above are initialized here
	{
		scr = new int[rows][columns];
		rot = 1;
		isPuyoDropped = true;
		count = 0;
		isGameStarted = false;
		isGameLost = false;
		isGamePaused = false;
		leftPuyo = 0;
		rightPuyo = 0;
		level = 0;
		score = 0;
		pieces = -1;
		puyosRemoved = 0;
		minscore = 50;
		anim = 0;
		alpha = 0.0f;
		alpha1 = 0.0f;
		levelflag = true;
		tk = Toolkit.getDefaultToolkit();
		rand = new Random();
		timer = new Timer(1000,this);	//generates action event for each 1075 milli seconds when timer is isGameStarted
		timer.setInitialDelay(0);	//generates first event ofter 0 ms when timer starts
		timer1 = new Timer(1000,this);
		timer2 = new Timer(500,this);
		anim_timer = new Timer(50,this);
		anim_timer.start();			//starting the timer
	}
	public void loadImages()//loading images into the image array
	{
		img[0] = tk.getImage("images\\puyo_blue.png");
		img[1] = tk.getImage("images\\puyo_red.png");
		img[2] = tk.getImage("images\\puyo_yellow.png");
		img[3] = tk.getImage("images\\puyo_green.png");
	}
	public void setDelays()
	{
		int delay = 0, delay1 = 0;
		for(int i = 0; i<=level; i++)//to set the delays depending on the level
		{
			delay += 20*(4-i/5);	
			delay1 += 4-i/5;
		}
		if(level == 20)
		{
			delay += 25;
			delay1 += 1;
		}
		timer.setDelay(1075-delay);
		anim_timer.setDelay(52-delay1);
		anim_timer.restart();
	}
	public void generatePuyos()//To generate Puyos at the top of the game window
	{
		
		//Checking Top of the game window is occupied by any puyos 
		//if occupied then game is over
		int p;
		if(columns%2 == 0)
		p = columns/2-1;
		else
		p = columns/2;
		if(scr[0][p] == 0 && scr[1][p] == 0)
		{
			scr[0][p] = leftPuyo;		//leftPuyo and rightPuyo are randomly generated Puyos
			scr[1][p] = rightPuyo;
		}
		else
		{
			timer.stop();		
			isGameLost=true;		//game is over
			return;
		}
		int r;					//Generating puyos randomly for the next fall which are viewed at the right side of window
		//Odd numbers 1,3,5,7(for 4 colors) are used for generating puyos which are in movement
		//Even numbers 2,4,6,6 are used for fallen puyos on bottom of the window
		while((r=rand.nextInt(8))%2==0);
		leftPuyo=r;
		while((r=rand.nextInt(8))%2==0);
		rightPuyo=r;
		pieces++;
		rot=1;
	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==timer)	//If Event is generated by the timer object
		{
			movePuyos();
		}
		else if(e.getSource()==timer1) //If Event is generated by the timer1 object
		{
			erase_puyos();
		}
		else if(e.getSource()==timer2)
		{
			fillVacated();			
			timer2.stop();
		}
		//timer1 and timer2 are used for make delay b/w erasing puyos and filling vacated places by puyos
		repaint();
	}
	public void movePuyos()//Moving generated puyos Downword
	{
		int flag=0;
		for(int i=rows-1;i>=0;i--)
		for(int j=0;j<columns;j++)
		if(scr[i][j]%2==1)
		{
			if(i==rows-1)	//for each moving puyo ie. on last row 12
			{
				scr[i][j]+=1;		//When generated puyo isPuyoDropped the ground ie. 12 row
				isPuyoDropped=true;		//then make it as fallen puyo by making it as even number
									//isPuyoDropped the ground
			}
			else if(scr[i+1][j]==0)		//if the next row is empty
			{
				scr[i+1][j]=scr[i][j];	//for each generated puyo in moment increase the row number 
				scr[i][j]=0;
				flag=1;			//to build the movement for that puyo
			}
			else
			{
				scr[i][j]+=1;		//If the next row contain any puyo then stop the movement	
				isPuyoDropped=true;		//of puyo by making it even
			}
			anim=0;			//for pixel by pixel animation of puyos here animation starts
		}
		if(flag==0)			//if flag is not set mean that there is no puyo is in moment
		erase_puyos();		//so erase puyo which form tetris
	}
	public void erase_puyos()
	{
		int flag=0;
		for(int i=0;i<rows;i++)
		for(int j=0;j<columns;j++)
		if(scr[i][j]>0)			//for all color puyos
		{
			count=1;
			tetris=new Node(i,j);//create a node for puyo 
			chkForTetris(i,j);	//check that node attached to the tetris puyos of same color
			if(count>=4)		//if tetris forms 
			{
				removeAllTetris();//remove the puyos which form tetris
				flag=1;
				///////////////////////////////////////
				//I made change here to remove the bug in this revised game (by moving 4 lines of code to the below if block) 
				//ie. erasing all the chain combos formed at a time will be removed at a time.
				///////////////////////////////////////
			}
		}
		if(flag==1)
		{
			timer.stop();	  //stop the generating puyos
			timer1.start();	 //erase puyos if there is any other form tetris with delay
			timer2.start();	 //fill vacated places of erased puyos with remain by the law of gravity with delay
			return;
		}	
		timer1.stop();			//If there is no puyos form tetris then stop timer for erasing puyos
		minscore=50;			//min score for each puyo is initiated
		generatePuyos();		//start generating puyos
		if(!timer.isRunning())
		timer.start();
	}
	public void chkForTetris(int x,int y)
	{
		if(y<columns-1 && scr[x][y]==scr[x][y+1] && !existsInTetris(x,y+1))
		{							//check for the same color puyo at the right side of 
			count++;				//current puyo which is not already added to the current tetris
			addToTetris(x,y+1);		//If there is then add that to the current tetris
			chkForTetris(x,y+1);	//Then check this node can connected to any same color puyo
		}
		if(x<rows-1 && scr[x][y]==scr[x+1][y] && !existsInTetris(x+1,y))
		{
			count++;
			addToTetris(x+1,y);		//check at the down side
			chkForTetris(x+1,y);
		}
		if(y>0 && scr[x][y]==scr[x][y-1] && !existsInTetris(x,y-1))
		{
			count++;
			addToTetris(x,y-1);		//check at the left side
			chkForTetris(x,y-1);
		}
		//I found a bug here and i rectified it by adding below if block
		//Before iam not checked up side
		//but if below type of chain combo formed by puyos
		//			* *
		//          ***
		//then up right puyo cannot be added to the tetris to remove
		if(x>0 && scr[x][y]==scr[x-1][y] && !existsInTetris(x-1,y))
		{
			count++;
			addToTetris(x-1,y);		//check at the up side
			chkForTetris(x-1,y);
		}
	}
	public void addToTetris(int x,int y)//adding another node to the present tetris
	{
		tetris.setNext(new Node(x,y));
		tetris.getNext().setPrev(tetris);//It is totally the linked list concept used here
		tetris=tetris.getNext();
	}
	public boolean existsInTetris(int x,int y)//comparing with the all the nodes in present tetris 
	{										//that it is already exists or not
		Node n=tetris;
		while(n!=null)
		{
			if(n.getX()==x && n.getY()==y)
			return true;
			n=n.getPrev();
		}
		return false;
	}
	public void removeAllTetris()
	{
		Node n=tetris;
		while(n!=null)
		{
			scr[n.getX()][n.getY()]=0;	//removing puyos which are in tetris
			n=n.getPrev();				
		}
		puyosRemoved+=count;
		if(puyosRemoved>=50)		//Change the level of the game depending on
		{								//number of removed puyos.
			if(levelflag)
			level+=1;
			else
			level-=1;				//Ofter playing the final level(here 20) the level decreased by 1
			if(level==20)			//It is decreased up to 15 and then it increases 20
			levelflag=false;		//So The Game is endlessly continued if player can play all levels perfectly without any minstake at any level.
			if(level==15 && !levelflag)
			levelflag=true;
			setDelays();
			puyosRemoved=0;
		}	
		score+=minscore*(count-3)*count;//Score is calculated by using this formula
		minscore=minscore*count;		//minscore in formula depends on the number of puyos formed in last tetris
										//Scoring is totally depends on the length of the chain and 
										//number of chains formed by puyos at a single time
	}
	public void fillVacated()	//vacated places formed by removed puyos are filled with the other puyos by the law of gravity
	{
		for(int i=rows-2;i>=0;i--)
		for(int j=0;j<columns;j++)
		if(scr[i][j]>0)				//for all puyos
		{
			int k;
			for(k=i+1;k<=rows-1;k++)
			if(scr[k][j]>0)			//any puyo exist below the current puyo up to the ground
			{
				scr[k-1][j]=scr[i][j]; //then move on to it
				if(i!=k-1)
				scr[i][j]=0;
				break;
			}
			else if(k==rows-1)		//if no puyo exist below the current puyo up to the ground
			{
				scr[rows-1][j]=scr[i][j];	//then move on to the ground
				if(i!=rows-1)
				scr[i][j]=0;
			}
		}
	}
	public void moveLeft()	//to move the coming down puyos one step left 
	{					
		for(int i=0;i<rows;i++)
		for(int j=0;j<columns;j++)
		if(scr[i][j]>0 && scr[i][j]%2==1 && j>0 )//for all the coming down puyos which are not in first column(or left column)
		{
			
			if(j<columns-1 && scr[i][j+1]%2==1 && scr[i][j-1]==0)	//if two puyo are in horizontal
			{													//if there is no puyo in left
					scr[i][j-1]=scr[i][j];		//move two puyo to one step left
					scr[i][j]=scr[i][j+1];
					scr[i][j+1]=0;
			}
			else
			if(scr[i][j-1]==0 && scr[i+1][j-1]==0 )//if two puyo are in vertical & there is no puyos in left
			{
				scr[i][j-1]=scr[i][j];
				scr[i+1][j-1]=scr[i+1][j];		//move two puyo to one step left
				scr[i][j]=0;
				scr[i+1][j]=0;
			}
			return;
		}
	}
	public void moveRight()	//to move the coming down puyos one step right
	{
		for(int i=0;i<rows;i++)
		for(int j=columns-1;j>=0;j--)
		if(scr[i][j]>0 && scr[i][j]%2==1 && j<columns-1)//for all the coming down puyos which are not in last column(or right column)
		{
			if(j>0 && scr[i][j-1]%2==1 && scr[i][j+1]==0)//if two puyo are in horizontal
			{											//if there is no puyo in right
					scr[i][j+1]=scr[i][j];
					scr[i][j]=scr[i][j-1];				//move two puyo to one step right
					scr[i][j-1]=0;
			}
			else
			if(scr[i][j+1]==0 && scr[i+1][j+1]==0 )//if two puyo are in vertical & there is no puyos in right
			{
				scr[i][j+1]=scr[i][j];
				scr[i+1][j+1]=scr[i+1][j];			//move two puyo to one step right
				scr[i][j]=0;
				scr[i+1][j]=0;
			}
			return;			
		}
	}
	public void rotate()	//to rotate the coming down puyos in clock wise direction by 90 degrees
	{
		for(int i=0;i<rows;i++)
		for(int j=0;j<columns;j++)
		if(scr[i][j]>0 && scr[i][j]%2==1)//for all the coming down puyos 
		{
			//alway consider the puyo left and top for horizontal and vertical positions as present
			//because checking is taking from top to bottom
			//Two puyos can make only four different positions rotating 90 degrees each time
			if(rot==1)	//first postion=vertical
			{			//moving down puyo to the left of first puyo makes 90 degrees rotaion to get second postion
				if(j>0 && scr[i][j-1]==0)//is left side is empty or not
				{
					scr[i][j-1]=scr[i+1][j];
					scr[i+1][j]=0;		//If it is empty location move there
					rot=2;				//change to second position
				}
				else if(j<columns-1 && scr[i][j+1]==0)//is left side is not empty, chk for right position
				{
					scr[i][j+1]=scr[i][j];//move present puyo to the right
					scr[i][j]=scr[i+1][j];//move down puyo to the present location
					scr[i+1][j]=0;
					rot=2;				//change to second position
				}
			}//second postion=horizontal
			else if(rot==2 && i>1)//moving present puyo to the up of right puyo makes 90 degrees rotaion to get third position
			{
				scr[i-1][j+1]=scr[i][j];//move present puyo to the up of the right puyo
				scr[i][j]=0;
				rot=3;					//change to third position
			}//third postion=vertcal (invert to the first postion in two puyos positions)
			else if(rot==3)			//moving present puyo to the up of right puyo makes 90 degrees rotaion to get fourth position
			{
				if(j<columns-1 && scr[i+1][j+1]==0)//is right side of the down puyo is empty or not
				{
					scr[i+1][j+1]=scr[i][j];	//If it is empty location move there
					scr[i][j]=0;				
					rot=4;						//change to fourth position
				}
				else if(j>0 && scr[i+1][j-1]==0)//is left side of the down puyo is empty or not
				{
					scr[i+1][j-1]=scr[i+1][j];	//move down puyo to the left
					scr[i+1][j]=scr[i][j];		//move present puyo to the down
					scr[i][j]=0;
					rot=4;						//change to fourth position
				}
			}//fourth position=horizontal (invert to the second postion in two puyos positions)
			else if(rot==4 && i<rows-1)//moving right puyo to the down of the current puyo makes 90 degrees rotaion to get first position
			{
				if(scr[i+1][j]==0)//is down position is empty or not
				{
					scr[i+1][j]=scr[i][j+1];//move right puyo to the down of the present puyo
					scr[i][j+1]=0;
					rot=1;					//change to first position
				}
			}
			return;
		}
	}
	public void moveDown()//Moving generated moving puyos by one step down
	{
		for(int i=rows-1;i>=0;i--)
		for(int j=0;j<columns;j++)
		if(scr[i][j]%2==1)//For all moving puyos
		{
			if(i==rows-1)	//if puyo is in last row
			{
				scr[i][j]=scr[i][j]+1;//making it as grounded
				isPuyoDropped=true;
			}
			else if(scr[i+1][j]>0 && scr[i+1][j]%2==0)//if next row of puyo contains another puyo
			{
				scr[i][j]=scr[i][j]+1;//then stop the current puyo at the current postion
				isPuyoDropped=true;
			}
			else 
			{
				scr[i+1][j]=scr[i][j];//Move present puyo one step down
				scr[i][j]=0;
			}
		}
		repaint();
	}
	public void paint(Graphics g)
	{
		g.setColor(Color.white);
		g.fillRect(0,0,len*columns,len*rows);//background fill with white color
		g.setColor(Color.black);
		g.fillRect(len*columns,0,len*3,len*rows);//Score board is filled with black color
		//Graphics2D g2=(Graphics2D)g;
		//g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
		//designing score board with gradient colors as borders
		//g2.setPaint(new GradientPaint(columns*len,0,new Color(50,50,50),columns*len+len/2,0,new Color(200,200,200),false));
		//g2.fill(new Rectangle(columns*len,0,len/2,rows*len));
		//g2.setPaint(new GradientPaint((columns+2)*len+len/2,0,new Color(200,200,200),(columns+3)*len,0,new Color(50,50,50),false));
		//g2.fill(new Rectangle((columns+2)*len+len/2,0,len/2,rows*len));
		//g2.setPaint(Color.red);
		//white background for next coming puyos display
		g.fill3DRect((columns+3/2)*len,len,len,len*2,true);
		//The next coming puyos showned or drawn here
		g.drawImage(img[leftPuyo/2],(columns+3/2)*len,len,len,len,null);
		g.drawImage(img[rightPuyo/2],(columns+3/2)*len,len*2,len,len,null);
		//Score board designed with 3 rectangled as backgrounds for
		//Level and Number of pices and Total Score
		g.fill3DRect((columns)*len+len/2,(rows+1)*len/2-5,len*2,len-2,true);
		g.fill3DRect((columns)*len+len/2,(rows+1)*len/2+len-5,len*2,len-2,true);
		g.fill3DRect((columns)*len+len/2,(rows+1)*len/2+2*len-5,len*2,len,true);
		//g2.setPaint(Color.black);
		//Font for the text to be display
		//g2.setFont(new Font("Ariel",Font.PLAIN,len/3));
		//g2.drawString("Level: "+level,(columns)*len+len/2,rows*len/2+len);
		//g2.drawString("pieces: "+pieces,(columns)*len+len/2,rows*len/2+2*len);
		//g2.drawString("Score:"+score,(columns)*len+len/2,rows*len/2+3*len);
		//back part of pipe is drawn as image 
		int p;
		if(columns%2==0)
		p=columns/2;
		else
		p=columns/2+1;
		for(int i=0;i<rows;i++)
		for(int j=0;j<columns;j++)
		if(scr[i][j]>0)//For all the puyos exists presently
		{
			int k=scr[i][j];
			if(k%2==0)//if puyo value is even then simply display it
			{
				k=(int)k/2;
				g.drawImage(img[k-1],j*len,i*len,len,len,null);
			}
			else
			{		//if puyo value is odd then add animation to it
				k=(int)k/2+1;
				g.drawImage(img[k-1],j*len,(i-1)*len+anim,len,len,null);
				anim+=2;
				if(anim>=len)
				anim=len;			
				if(isPuyoDropped && i==2)
				isPuyoDropped=false;
			}
			
		}
				
		if(!isGameStarted)//if game is not started display the information
		{
			//g2.setPaint(Color.yellow);
			//g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f));
			//g2.fill(new Rectangle(0,rows*len/4,(columns+3)*len,(rows+1)*len/2));
			//g2.setPaint(Color.blue);
			//g2.setFont(new Font("Ariel",Font.PLAIN,len/2));
			//g2.drawString("  Level: "+level,len*3,rows*len/3);
			//g2.setFont(new Font("Ariel",Font.PLAIN,len/3));
			//g2.drawString("Use the <up> and <down> arrow keys to change level now",len/4,(rows+2)*len/3); 
			//g2.setFont(new Font("Ariel",Font.PLAIN,len/2));
			//g2.drawString(" Press <Enter> to start the Game",len,rows*len/2);
			//g2.setFont(new Font("Ariel",Font.PLAIN,len/3));
			//g2.drawString("   Use the left,right and down arrow keys to move the puyos.",0,(rows+1)*len/2); 
			//g2.drawString("   Pressing the up arrow key rotates the piece.",len,(rows+2)*len/2);
			//g2.drawString("  When 4 or more puyos of the same colour are touching",len/3,(rows+3)*len/2);
			//g2.drawString("                       they disappear. ",len,(rows+4)*len/2);
			//g2.drawString("              Press <p> to pause the Game",len,(rows+5)*len/2);
			//g2.drawString("            Press <Escape> to exit the Game",len,(rows+6)*len/2);
		}
		if(isGameLost)//if game is over dim the game by using alpha composite and display the information
		{
			//g2.setPaint(Color.white);
			//g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
			if(alpha<0.9f)
			alpha=alpha+0.02f;//to build the animation of alpha blending
			//g2.fill(new Rectangle(0,0,len*columns,len*rows));
			//g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
			//g2.setPaint(Color.red);
			//g2.setFont(new Font("Ariel",Font.PLAIN,len/2));
			//g2.drawString("Game Over",len*3/2,rows*len/2);
			//g2.setPaint(Color.blue);
			//g2.setFont(new Font("Ariel",Font.PLAIN,len/3));
			//g2.drawString("Press <Enter> to restart the Game",len/2,(rows+1)*len/2);
		}
		if(isGamePaused)//if game is paused dim the game by using alpha composite and display the information
		{
			//g2.setPaint(Color.white);
			//g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha1));
			if(alpha1<0.9f)
			alpha1=alpha1+0.02f;
			//g2.fill(new Rectangle(0,0,len*columns,len*rows));
			//g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
			//g2.setPaint(Color.blue);
			//g2.setFont(new Font("Ariel",Font.PLAIN,len/2));
			//g2.drawString("Game Paused",len*3/2,rows*len/2);
			//g2.setFont(new Font("Ariel",Font.PLAIN,len/3));
			//g2.drawString("   Press <p> to resume the Game",len/2,(rows+1)*len/2);
			//g2.drawString("  Press <Escape> to exit the Game",len/2,(rows+2)*len/2);
			//g2.drawString(" Press <Enter> to restart the Game",len/2,(rows+3)*len/2);
		}
	}		
}
class Node
{
	//linked list is bulilt for the tetris of puyos to store their positions
	int x,y;
	Node nextnode;
	Node prevnode;
	public Node(int x,int y)
	{
		this.x=x;
		this.y=y;
		nextnode=null;
		prevnode=null;
	}
	public Node(Node p)
	{
		x=p.x;
		y=p.y;
		nextnode=null;
		prevnode=null;
	
	}
	public void setNext(Node lnode)
	{
		nextnode=lnode;
	}
	public Node getNext()
	{
		return nextnode;
	}
	public void setPrev(Node lnode)
	{
		prevnode=lnode;
	}
	public Node getPrev()
	{
		return prevnode;
	}
	public int getX()
	{
		return x;
	}
	public int getY()
	{
		return y;
	}
}
