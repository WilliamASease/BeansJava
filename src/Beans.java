import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;

@SuppressWarnings("serial")
public class Beans extends JPanel {
	// Unchanging Fields
	HashMap<String, Image> sprites = readInAll();
	ArrayList<Image> guysprites = readInGuys();
	Random random = new Random();
	File[] audiofiles;
	AudioFormat[] fmt;
	AudioInputStream[] strm;
	SourceDataLine[] dta;

	// Fields used by the launcher
	Launcher l;
	boolean start = false;
	String backgroundMusic = "";
	double difficulty = 0;

	// Fields that may change
	int[][] board = new int[6][16];
	int[] central = new int[2];
	int orbit = 0;
	int[] orbitPos = new int[2];
	int[] falling = new int[2];
	boolean[][] checked;
	boolean paintFalling = true;
	boolean playerIn = false;
	int popped = 0;
	int poppedtemp = 0;
	int score = 0;
	int chain = 0;
	int[] next;
	int[] nextpos = getNextPos();

	public Beans() {
		Launcher l = new Launcher();
		while (!start) {
			wait(.02);
		}
		setLocation(500, 100);
		setSize(800, 900);
		setVisible(true);
		JFrame frame = new JFrame();
		frame.setTitle("Beans Java");
		frame.setLocation(500, 100);
		frame.setSize(810, 920);
		frame.setAlwaysOnTop(true);
		frame.setVisible(true);
		frame.add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addKeyListener(getKeyListener());
		loadAudio();
		playAudio(backgroundMusic, true);
		initBoard();
		playerPhase();
	}

	public static void main(String[] args) {
		Beans b = new Beans();
	}

	public void paint(Graphics g) {
		super.paint(g);
		g.drawImage(sprites.get("InGameBasic.png"), 0, 0, this);
		if (paintFalling) {
			g.drawImage(guysprites.get(falling[0]), 50 * central[0] + 45, 50 * central[1] + 49, this);
			g.drawImage(guysprites.get(falling[1]), 50 * orbitPos[0] + 45, 50 * orbitPos[1] + 49, this);
		}
		g.drawString(score + " ", 550, 450);
		g.drawString(popped + " ", 550, 550);
		g.drawString(poppedtemp + " * " + chain + " = " + poppedtemp * chain, 550, 650);
		g.drawString(difficulty + " ", 550, 750);
		try {
		for (int i = 0; i < next.length; i++)
			g.drawImage(guysprites.get(next[i]), nextpos[i * 2], nextpos[i * 2 + 1], this);
		}
		catch (NullPointerException e) {}
		for (int i = 0; i < 6; i++)
			for (int j = 0; j < 16; j++) {
				if (board[i][j] != -1) {
					g.drawImage(guysprites.get(board[i][j]), 50 * i + 45, 50 * j + 49, this);
				}
			}
	}

	public void drawStringWithSprites(Graphics g) {

	}

	public HashMap<String, Image> readInAll()

	{
		HashMap<String, Image> out = new HashMap<String, Image>();
		File folder = new File("sprites");
		File[] sprites = folder.listFiles();
		for (int i = 0; i < sprites.length; i++) {
			try {
				BufferedImage image = ImageIO.read(sprites[i]);
				out.put(sprites[i].getName(), image);
			} catch (IOException e) {
				System.err.println("Error in ReadInall()");
				e.printStackTrace();
			}
		}
		return out;
	}

	public ArrayList<Image> readInGuys()

	{
		ArrayList<Image> out = new ArrayList<Image>();
		File folder = new File("sprites");
		File[] sprites = folder.listFiles();
		for (int i = 0; i < sprites.length; i++) {
			try {
				BufferedImage image = ImageIO.read(sprites[i]);
				if (sprites[i].getName().substring(0, 3).equals("Guy"))
					out.add(image);
			} catch (IOException e) {
				System.err.println("Error in ReadInall()");
				e.printStackTrace();
			}
		}
		return out;
	}

	public void initBoard() {
		for (int i = 0; i < board.length; i++)
			for (int j = 0; j < board[i].length; j++)
				board[i][j] = -1;
		next = new int[12];
		for (int i = 0; i < next.length; i++) {
			next[i] = random.nextInt(guysprites.size());
		}
	}

	public int[] getNextPos() {
		int x = 362;
		int y1 = 45;
		int y2 = 95;
		int[] out = new int[24];
		for (int i = 0; i < out.length; i += 4) {
			out[i] = x;
			out[i + 1] = y1;
			out[i + 2] = x;
			out[i + 3] = y2;
			x += 50;
		}
		return out;
	}

	public void getNext() {
		falling[0] = next[0];
		falling[1] = next[1];
		for (int i = 2; i < next.length; i++)
			next[i - 2] = next[i];
		next[10] = random.nextInt(guysprites.size());
		next[11] = random.nextInt(guysprites.size());
	}

	public void wait(double seconds) {
		long l = System.currentTimeMillis();
		while (System.currentTimeMillis() < l + (seconds * 1000)) {
		}
	}

	public void playerPhase() {
		playerIn = false;
		getNext();
		orbit = 2;
		central = new int[] { 2, 0 };
		orbitPos = getOrbitPos();
		if ((double) popped / 100.0 > difficulty)
			difficulty = (double) popped / 100.0;
		repaint();
		fall();
	}

	public void fall() {
		wait(0.5 / difficulty);
		playerIn = true;
		if (collision('d'))
			compPhase();
		else {
			central[1]++;
			orbitPos = getOrbitPos();
			paintFalling = true;
			repaint();
			fall();
		}
	}

	public int[] getOrbitPos() {
		switch (orbit) {
		case 0:
			return new int[] { central[0], central[1] - 1 };
		case 1:
			return new int[] { central[0] + 1, central[1] };
		case 2:
			return new int[] { central[0], central[1] + 1 };
		case 3:
			return new int[] { central[0] - 1, central[1] };
		default:
			System.err.println("Error in getOrbitPos!!!");
			return new int[] { 0, 0 };
		}
	}

	public boolean collision(char d) {
		int centtemp;
		int orbittemp;
		int torbit[] = new int[2];
		int temp;
		switch (d) {
		case 'd':
			centtemp = central[1] + 1;
			orbittemp = orbitPos[1] + 1;
			return (centtemp > 15 || orbittemp > 15 || board[central[0]][centtemp] != -1
					|| board[orbitPos[0]][orbittemp] != -1);
		case 'l':
			centtemp = central[0] - 1;
			orbittemp = orbitPos[0] - 1;
			return (centtemp < 0 || orbittemp < 0 || board[centtemp][central[1]] != -1
					|| board[orbittemp][orbitPos[1]] != -1);
		case 'r':
			centtemp = central[0] + 1;
			orbittemp = orbitPos[0] + 1;
			return (centtemp > 5 || orbittemp > 5 || board[centtemp][central[1]] != -1
					|| board[orbittemp][orbitPos[1]] != -1);
		case 'z':
			temp = orbit;
			if (orbit == 0)
				orbit = 3;
			else
				orbit = (orbit - 1) % 4;
			torbit = getOrbitPos();
			orbit = temp;
			return (torbit[0] < 0 || torbit[0] > 5 || torbit[1] < 0 || torbit[1] > 15
					|| board[torbit[0]][torbit[1]] != -1);
		case 'x':
			temp = orbit;
			orbit = (orbit + 1) % 4;
			torbit = getOrbitPos();
			orbit = temp;
			return (torbit[0] < 0 || torbit[0] > 5 || torbit[1] < 0 || torbit[1] > 15
					|| board[torbit[0]][torbit[1]] != -1);
		}
		return false;
	}

	public KeyListener getKeyListener() {
		KeyListener k = new KeyListener() {
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_DOWN && !collision('d') && playerIn) {
					central[1]++;
				}
				if (event.getKeyCode() == KeyEvent.VK_LEFT && !collision('l') && playerIn) {
					central[0]--;
				}
				if (event.getKeyCode() == KeyEvent.VK_RIGHT && !collision('r') && playerIn) {
					central[0]++;
				}
				if (event.getKeyCode() == KeyEvent.VK_Z && !collision('z') && playerIn) {
					if (orbit == 0)
						orbit = 3;
					else
						orbit = (orbit - 1) % 4;
				}
				if (event.getKeyCode() == KeyEvent.VK_X && !collision('x')) {
					orbit = (orbit + 1) % 4;
				}
				orbitPos = getOrbitPos();
				repaint();
			}

			@Override
			public void keyReleased(KeyEvent arg0) {

			}

			@Override
			public void keyTyped(KeyEvent arg0) {

			}
		};
		return k;
	}

	public void compPhase() {
		poppedtemp = 0;
		chain = 0;
		board[central[0]][central[1]] = falling[0];
		board[orbitPos[0]][orbitPos[1]] = falling[1];
		shuffleBoard();
		checkFail();
		popped += poppedtemp;
		score += poppedtemp * chain;
		playerPhase();
	}

	public void shuffleBoard() {
		for (int i = 0; i < 6; i++)
			for (int j = 14; j > -1; j--) {
				if (board[i][j] != -1 && board[i][j + 1] == -1) {
					int toPlace = board[i][j];
					board[i][j] = -1;
					int fall = j;
					while (fall < 14 && board[i][fall + 2] == -1)
						fall++;
					board[i][fall + 1] = toPlace;
				}
			}
		paintFalling = false;
		repaint();
		wait(0.3);
		popBoard();
	}

	public void popBoard() {
		boolean needToShuffle = false;
		checked = new boolean[6][16];
		boolean[][] popIt = new boolean[6][16];
		for (int i = 0; i < 6; i++)
			for (int j = 0; j < 16; j++) {
				if (board[i][j] != -1 && !checked[i][j]) {
					int[] scanned = scan(i, j, board[i][j]);
					if (scanned.length >= 8)
						for (int k = 0; k < scanned.length; k += 2) {
							popIt[scanned[k]][scanned[k + 1]] = true;
							poppedtemp++;
							needToShuffle = true;
						}
				}
			}
		for (int i = 0; i < 6; i++)
			for (int j = 0; j < 16; j++) {
				if (popIt[i][j])
					board[i][j] = -1;
			}
		paintFalling = false;
		repaint();
		wait(0.2);
		if (needToShuffle) {
			playAudio("pop_default.wav", false);
			chain++;
			shuffleBoard();
		}
	}

	public int[] scan(int i, int j, int tgt) {
		if (i < 0 || i > 5 || j < 0 || j > 15 || checked[i][j] || board[i][j] != tgt)
			return new int[0];
		checked[i][j] = true;
		int[] up = scan(i, j - 1, tgt);
		int[] down = scan(i, j + 1, tgt);
		int[] left = scan(i - 1, j, tgt);
		int[] right = scan(i + 1, j, tgt);
		int out[] = new int[2];
		out[0] = i;
		out[1] = j;
		int[] updown = arrayConcatenate(up, down);
		int[] leftright = arrayConcatenate(left, right);
		int[] all = arrayConcatenate(updown, leftright);
		out = arrayConcatenate(out, all);
		return out;
	}

	public int[] arrayConcatenate(int[] var1, int[] var2) {
		int ctr = 0;
		int[] out = new int[var1.length + var2.length];
		for (int i = 0; i < var1.length; i++) {
			out[i] = var1[i];
			ctr++;
		}
		for (int i = 0; i < var2.length; i++) {
			out[ctr] = var2[i];
			ctr++;
		}
		return out;
	}

	public void checkFail() {
		for (int i = 0; i < 6; i++)
			for (int j = 0; j < 4; j++) {
				if (board[i][j] != -1) {
					failure();
				}
			}
	}
	
	public void failure()
	{
		JFrame end = new JFrame();
		end.setLocation(700, 400);
		end.setSize(200, 200);
		end.setAlwaysOnTop(true);
		end.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		panel.setVisible(true);
		end.add(panel);
		JTextField scoreLabel = new JTextField("SCORE: " + score);
		JTextField beansLabel = new JTextField("BEANS CLEARED: " + popped);
		JTextField diffLabel = new JTextField("DIFFICULTY REACHED: " + difficulty);
		scoreLabel.setVisible(true);
		beansLabel.setVisible(true);
		diffLabel.setVisible(true);
		scoreLabel.setEditable(false);
		beansLabel.setEditable(false);
		diffLabel.setEditable(false);
		panel.add(scoreLabel);
		panel.add(beansLabel);
		panel.add(diffLabel);
		panel.repaint();
		end.setVisible(true);
		while (true) {}
	}

	public void loadAudio() {
		try {

			File folder = new File("sounds");
			audiofiles = folder.listFiles();
			fmt = new AudioFormat[audiofiles.length];
			strm = new AudioInputStream[audiofiles.length];
			dta = new SourceDataLine[audiofiles.length];
			for (int i = 0; i < audiofiles.length; i++) {
				strm[i] = AudioSystem.getAudioInputStream(audiofiles[i]);
				fmt[i] = strm[i].getFormat();
				DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt[i]);
				dta[i] = (SourceDataLine) AudioSystem.getLine(info);
			}
		} catch (Exception e) {
			System.err.println("Exceptional!");
		}
	}

	public void playAudio(String tgt, boolean forever) {
		for (int i = 0; i < audiofiles.length; i++) {
			if (audiofiles[i].getName().equals(tgt)) {
				PlayThread thread = new PlayThread(i, forever);
				thread.start();
			}
		}
	}

	class PlayThread extends Thread {
		int cat;
		boolean forever;

		public PlayThread(int cat, boolean forever) {
			this.cat = cat;
			this.forever = forever;
		}

		public void run() {
			boolean justonce = true;
			while (forever || justonce) {
				try {
					strm[cat] = AudioSystem.getAudioInputStream(audiofiles[cat]);
					fmt[cat] = strm[cat].getFormat();
					DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt[cat]);
					dta[cat] = (SourceDataLine) AudioSystem.getLine(info);
					byte[] tempBuffer = new byte[10000];
					dta[cat].open(fmt[cat]);
					dta[cat].start();
					int cnt;
					while ((cnt = strm[cat].read(tempBuffer, 0, tempBuffer.length)) > 0) {
						if (cnt > 0)
							dta[cat].write(tempBuffer, 0, cnt);
					}
					justonce = false;
				} catch (Exception e) {
					System.err.println("Exceptional");
					e.printStackTrace();
				}
			}
		}
	}

	class Launcher extends JPanel {
		JFrame frame;

		public Launcher() {
			frame = new JFrame();
			frame.setSize(200, 500);
			frame.setLocation(900, 300);
			frame.setAlwaysOnTop(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.add(this);

			// Add the song label.
			JLabel songlabel = new JLabel("Select a song:");
			this.add(songlabel);

			// List all the songs.
			File folder = new File("sounds");
			File[] sounds = folder.listFiles();
			ArrayList<File> songs = new ArrayList<File>();
			for (int i = 0; i < sounds.length; i++) {
				if (sounds[i].getName().substring(0, 5).equals("song_"))
					songs.add(sounds[i]);
			}
			String[] songnames = new String[songs.size()];
			for (int i = 0; i < songs.size(); i++)
				songnames[i] = songs.get(i).getName();
			@SuppressWarnings({ "rawtypes", "unchecked" })
			JComboBox cb = new JComboBox(songnames);
			cb.setSize(260, cb.getHeight());
			cb.setEditable(false);
			cb.setSelectedIndex(random.nextInt(songnames.length));
			this.add(cb);

			// Add the difficulty label.
			JLabel diffLabel = new JLabel("Select a difficulty:");
			this.add(diffLabel);

			// List all the difficulties.
			String[] diffs = { "Wimpy", "Touchy", "Learning", "Confident", "Cocky" };
			@SuppressWarnings({ "rawtypes", "unchecked" })
			JComboBox difBox = new JComboBox(diffs);
			difBox.setEditable(false);
			difBox.setSelectedIndex(random.nextInt(diffs.length));
			this.add(difBox);

			// Add the GO button.
			JButton go = new JButton("Go");
			this.add(go);
			go.addActionListener(getActionListener(cb, difBox));

			this.setVisible(true);
			frame.setVisible(true);
		}

		public ActionListener getActionListener(JComboBox<String> song, JComboBox<String> diff) {
			return new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					backgroundMusic = song.getItemAt(song.getSelectedIndex());
					difficulty = (double) diff.getSelectedIndex() + 1;
					frame.dispose();
					start = true;
				}
			};
		}
	}
}
