package SwingTest;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import SwingTest.MyPoint;
import SwingTest.EditorEx.DrawingPanel;
import SwingTest.EditorEx.ServerThread;

public class ClientEx extends JFrame {

	private Color currentColor = Color.BLACK; 
	private DrawingPanel drawPane = null;
	private int penWidth = 3;
    private String answer = "";
    private BufferedReader in = null;
	private BufferedWriter out = null;
	private Socket socket = null;
	private DefaultListModel<String> model = new DefaultListModel<>();
	private JScrollPane listScrollPane;
	private JTextArea consolePane;
	private Vector<Vector<MyPoint>> splineList;
	private Vector<Vector<MyPoint>> erasedSplineList;
	private Vector<MyPoint> currentSpline = null;
	
    public ClientEx() {
    	
    	setTitle("Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //layout
        Container c = getContentPane();
        
        c.setLayout(new BorderLayout());

        drawPane = new DrawingPanel();
        
        //설레는 댓글창
        this.consolePane = new JTextArea();
        JScrollPane consoleScrollPane = new JScrollPane(this.consolePane);
        consoleScrollPane.setPreferredSize(new Dimension(200, 100));
        consoleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        c.add(consoleScrollPane, BorderLayout.SOUTH); 
        
        model = new DefaultListModel<>();
        
		JList<String> list = new JList<>(model);
		list.setEnabled(false);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setFixedCellWidth(150);  
		model.addElement("***************************************");
		model.addElement("정답을 입력해주세요.");
		model.addElement("***************************************");
		
		this.listScrollPane = new JScrollPane(list);
		this.listScrollPane.setPreferredSize(new Dimension(200, 100));
        this.listScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		c.add(this.listScrollPane, BorderLayout.EAST);
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(listScrollPane, BorderLayout.CENTER);
		rightPanel.add(consoleScrollPane, BorderLayout.SOUTH);

        drawPane.setBackground(Color.WHITE);
		c.add(rightPanel, BorderLayout.EAST);
        c.add(drawPane, BorderLayout.CENTER);
        
		pack(); 
		setSize(1024, 712);
        setVisible(true);
        

        new ClientThread().start();
        
    }

    class ClientThread extends Thread {
    	 @Override
         public void run() {
             try {
                 socket = new Socket("localhost", 9999);
                 SwingUtilities.invokeLater(() -> model.addElement("Server와 연결되었습니다."));

                 in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                 
                 new ReceiveThread(in).start();
//                 new DrawingThread(in).start();
                 
                 InputMap im = consolePane.getInputMap(JComponent.WHEN_FOCUSED);
                 ActionMap am = consolePane.getActionMap();
                 
   
                 im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterPressed");
                 am.put("enterPressed", new AbstractAction() {
                   @Override
                   public void actionPerformed(ActionEvent e) {
       
                       String text = consolePane.getText();
                       
                       try {
                          
                    	   model.addElement("Client: " + text);
                    	   consolePane.setText(""); // 입력창 초기화
                           
                           if (out != null) {
                        	   if (text.equals(answer)) {
	                           		model.addElement("***************************************");
	                        		model.addElement("Client가 정답을 맞췄습니다!");
	                        		model.addElement("***************************************");
	    	                    	if (splineList != null) splineList.clear();
	    	                    	if (currentSpline != null) currentSpline.clear();
	    	                    	drawPane.repaint();
                        	   }
                        	   out.write(text + "\n");
                        	   out.flush();
                           }
                           
                           SwingUtilities.invokeLater(() -> {
                               JScrollBar bar = listScrollPane.getVerticalScrollBar();
                               bar.setValue(bar.getMaximum());
                           });
       	                    
                       } catch (IOException ele) {
                       }
                   }
               });
             } catch (IOException e) {
             }
         }
    }
    
    class ReceiveThread extends Thread {
    	BufferedReader in;
    	
    	ReceiveThread(BufferedReader in) {
    		this.in = in;
    	}
    	
    	@Override
    	public void run() {
    		try {
    			while(true) {
    				final String receivedData = in.readLine();
                    if (receivedData == null) break;
                    SwingUtilities.invokeLater(() -> {
                        System.out.println(receivedData);
	                    if (receivedData.startsWith("PRESSED") || receivedData.startsWith("DRAGGING")) {
	                        String[] token = receivedData.split(" ");
	                    	MyPoint p = new MyPoint(Integer.parseInt(token[1]), Integer.parseInt(token[2]));
	                    	p.setColor(new Color(Integer.parseInt(token[3]), false));
	            	   		p.setWidth(Integer.parseInt(token[4]));
	            	   		if (receivedData.startsWith("PRESSED")) {
	            	   			currentSpline = new Vector<>();
	            	   			splineList.add(currentSpline);
	            	   		}
	            	   		currentSpline.add(p);
	            	   		drawPane.repaint();
	                    } else if (receivedData.equals("UNDO")) {
	                		erasedSplineList.add(splineList.get(splineList.size()-1));
	                		splineList.remove(splineList.size()-1);
	                    	drawPane.repaint();
	                    } else if (receivedData.equals("REDO")) {
	                		splineList.add(erasedSplineList.get(erasedSplineList.size()-1));
	                		erasedSplineList.remove(erasedSplineList.size()-1);
	                    	drawPane.repaint();
	                    } else if (receivedData.equals("ERASEALL")) {
	                    	if (splineList != null) splineList.clear();
	                    	if (currentSpline != null) currentSpline.clear();
	                    	drawPane.repaint();
	                    }
	                    else {
	                        ClientEx.this.answer = receivedData;
	                    }
                    });
    			}
                
    		} catch (IOException e) {
    			
    		}
    	}
    }

    //개중요
    class DrawingPanel extends JPanel{
   	
    	
    	MyPoint start = null; 
    	MyPoint end = null; 
    	
    	DrawingPanel(){
    		splineList = new Vector<Vector<MyPoint>>();
    		erasedSplineList = new Vector<Vector<MyPoint>>();
    		start = new MyPoint();
    		end = new MyPoint();
    	}
    	
    	public void paintComponent(Graphics g) {
    		super.paintComponent(g);
			
			for(Vector<MyPoint> spline : splineList) {
				Graphics2D g2 = (Graphics2D) g;
				
				if (spline.size() == 1) {
					g.setColor(spline.get(0).pointColor);
		    		g2.setStroke(new BasicStroke(spline.get(0).width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					g.drawLine(spline.get(0).x, spline.get(0).y, spline.get(0).x, spline.get(0).y);
					
					continue;
				}
				
				for (int i = 1; i < spline.size(); i++) {
					MyPoint temp1 = spline.get(i-1);
					MyPoint temp2 = spline.get(i);
					g.setColor(temp1.pointColor);
		    		g2.setStroke(new BasicStroke(temp1.width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					g.drawLine(temp1.x, temp1.y, temp2.x, temp2.y);
				}
			}

			
    	}

    }	
    
    
    
    public static void main(String[] args) {
        new ClientEx();
    }
}