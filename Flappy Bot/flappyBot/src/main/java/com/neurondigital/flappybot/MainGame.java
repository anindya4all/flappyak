package com.neurondigital.flappybot;

import java.util.ArrayList;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.MotionEvent;

import com.neurondigital.nudge.Button;
import com.neurondigital.nudge.HighScoreManager;
import com.neurondigital.nudge.Instance;
import com.neurondigital.nudge.Physics;
import com.neurondigital.nudge.Screen;
import com.neurondigital.nudge.Sprite;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class MainGame extends Screen {

	//paints
	Paint background_shader = new Paint();
	Paint Title_Paint = new Paint();
	Paint SubTitle_Paint = new Paint();
	Paint Score_Paint = new Paint();
	Paint Instruction_Paint = new Paint();
	Paint Black_shader = new Paint();
	Paint White_shader = new Paint();
	Paint Yellow_shader = new Paint();
	Paint Grey_shader = new Paint();

	//background
	Bitmap background;

	//instances
	ArrayList<Instance> clouds = new ArrayList<Instance>();
	Instance background_building_1, background_building_2;
	Sprite cloud_sprite, cloud_sprite2, column_edge;
	Instance bird;

	//physics
	Physics physics = new Physics();

	//states
	final int MENU = 0, GAMEPLAY = 1, HIGHSCORES = 2, GAMEOVER = 3;
	int state = MENU;
	boolean pause = false, notstarted = true;

	//menu buttons
	Button btn_Play, btn_Highscores, btn_Exit, btn_Home, btn_facebook, btn_Replay, btn_sound_mute, btn_music_mute, btn_pause;
	Sprite play_btn_sprite, pause_btn_sprite;

	//score
	int score = 0;
	HighScoreManager highscoreManager;
	HighScoreManager.Highscore[] highscore_list;
	Sprite score_cup;

	//sound
	SoundPool sp;
	MediaPlayer music;
	int sound_score, sound_fall, sound_beep;
	boolean sound_muted = false, music_muted = false;
	Sprite sound_on, sound_off, music_on, music_off;

	//Colors
	//TODO: Feel free to change these colors
	final int BLACK = Color.argb(255, 51, 51, 51);
	final int RED = Color.argb(255, 243, 120, 93);
	final int WHITE = Color.argb(255, 242, 242, 242);
	final int YELLOW = Color.argb(255, 253, 220, 81);
	final int GREY = Color.argb(255, 128, 128, 128);



	//game over counter
	int gameover_counter = 0;
	boolean game_over = false;

	//columns
	final int X = 0, Y = 1;
	float hole_size;
	int next_column_to_create = 0;

	//TODO: variables you can change to control game speed, delays...
	int touch_speed = 15;//the speed the bird would move up when screen touched.
	int gameover_delay = 20;
	int hole_size_ComparedToBird = 4;
	int gravity = 15;

	//ad
	private InterstitialAd interstitial;
	int ad_counter = 0;
	AdRequest adRequest;

	//TODO: Change the hole positions and column positions from here
	//column positions
	//this array represents the holes in the columns. 
	final float column_positions[][] = new float[][] {
			//{X coordinate of column, Y Coordinate of hole, where 0.5f is center of screen}
			{ 600, 0.5f },
			{ 900, 0.3f },
			{ 1200, 0.2f },
			{ 1500, 0.3f },
			{ 1800, 0.1f },
			{ 2100, 0.4f },
			{ 2400, 0.5f },
			{ 4000, 0.8f },
			{ 3000, 0.2f },
			{ 3300, 0.1f },
			{ 3600, 0.4f },
			{ 3900, 0.5f },
			{ 4200, 0.4f },
			{ 4500, 0.3f },
			{ 4800, 0.1f },
			{ 5100, 0.3f },
			{ 5400, 0.8f },
			{ 5700, 0.4f },
			{ 6000, 0.5f },
			{ 6300, 0.1f },
			{ 6600, 0.4f },
			{ 6900, 0.1f },
			{ 7200, 0.6f },
			{ 7500, 0.4f },
			{ 7800, 0.6f },
			{ 8100, 0.8f },
			{ 8400, 0.1f },
			{ 8700, 0.9f },
			{ 9000, 0.5f },
			{ 9300, 0.6f },
			{ 9600, 0.2f },
			{ 9900, 0.6f },
			{ 10200, 0.8f },
			{ 10500, 0.1f },
			{ 10800, 0.9f },
			{ 11100, 0.2f },
			{ 11400, 0.8f },
			{ 11700, 0.1f },
			{ 12000, 0.9f },
			{ 12300, 0.2f },
			{ 12600, 0.8f },
			{ 12900, 0.3f },
			{ 13200, 0.9f },
			{ 13500, 0.1f }
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		//setDebugMode(true);
		initialiseAccelerometer();

		if (getResources().getString(R.string.InterstitialAd_unit_id).length() > 0) {
			// Create the interstitial
			interstitial = new InterstitialAd(this);
			interstitial.setAdUnitId(getResources().getString(R.string.InterstitialAd_unit_id));

			// Create ad request.
			adRequest = new AdRequest.Builder()
					.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
					.addTestDevice("275D94C2B5B93B3C4014933E75F92565")///nexus7//////testing
					.addTestDevice("91608B19766D984A3F929C31EC6AB947") /////////////////testing//////////////////remove///////////
					.addTestDevice("6316D285813B01C56412DAF4D3D80B40") ///test htc sensesion xl
					.addTestDevice("8C416F4CAF490509A1DA82E62168AE08")//asus transformer
					.addTestDevice("7B4C6D080C02BA40EF746C4900BABAD7")//Galaxy S4
					.build();

			// Begin loading your interstitial.
			//interstitial.loadAd(adRequest);
		}
		//initialise banner ad
		this.BANNER_AD_UNIT_ID = getResources().getString(R.string.BannerAd_unit_id);
		showBanner();

		//highscores
		highscoreManager = new HighScoreManager(this, savedInstanceState, layout);

	}

	public void openAd() {
		if (getResources().getString(R.string.InterstitialAd_unit_id).length() > 0) {
			runOnUiThread(new Runnable() {
				public void run() {
					if (!interstitial.isLoaded()) {
						interstitial.loadAd(adRequest);
					}
					interstitial.setAdListener(new AdListener() {
						public void onAdLoaded() {
							interstitial.show();
						}

					});

				}
			});
		}
	}

	@Override
	public void Start() {
		super.Start();
		//fonts
		Typeface SCRIPTBL = Typeface.createFromAsset(getAssets(), "SCRIPTBL.TTF");

		//set paints
		//title
		Title_Paint.setTextSize(dpToPx(60));
		Title_Paint.setAntiAlias(true);
		Title_Paint.setColor(BLACK);
		Title_Paint.setTypeface(SCRIPTBL);

		//subtitle
		SubTitle_Paint.setTextSize(dpToPx(20));
		SubTitle_Paint.setAntiAlias(true);
		SubTitle_Paint.setColor(BLACK);
		SubTitle_Paint.setTypeface(Typeface.DEFAULT_BOLD);

		//score Paint
		Score_Paint.setTextSize(dpToPx(50));
		Score_Paint.setAntiAlias(true);
		Score_Paint.setColor(RED);
		Score_Paint.setTypeface(SCRIPTBL);

		//Instruction Paint
		Instruction_Paint.setTextSize(dpToPx(50));
		Instruction_Paint.setAntiAlias(true);
		Instruction_Paint.setColor(BLACK);
		Instruction_Paint.setTypeface(SCRIPTBL);

		Black_shader.setColor(BLACK);
		White_shader.setColor(WHITE);
		Yellow_shader.setColor(YELLOW);
		Grey_shader.setColor(GREY);

		//get menu ready
		//play button
		btn_Play = new Button(new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.play), ScreenHeight() * 0.3f), 0, 0, this, false);
		btn_Play.x = (ScreenWidth() / 2) - btn_Play.getWidth() / 2;
		btn_Play.y = (ScreenHeight() / 2) - btn_Play.getHeight() / 2;

		//highscores button
		btn_Highscores = new Button(new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.highscores), ScreenWidth() * 0.1f), 0, 0, this, false);
		btn_Highscores.x = btn_Highscores.getWidth() * 0.2f;
		btn_Highscores.y = ScreenHeight() - btn_Highscores.getHeight() * 1.2f;

		//exit button
		btn_Exit = new Button(new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.exit), ScreenWidth() * 0.1f), 0, 0, this, false);
		btn_Exit.x = ScreenWidth() - btn_Exit.getWidth() * 1.2f;
		btn_Exit.y = ScreenHeight() - btn_Exit.getHeight() * 1.2f;

		//home button
		btn_Home = new Button(new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.home), ScreenWidth() * 0.1f), 0, 0, this, false);
		btn_Home.x = ScreenWidth() - btn_Home.getWidth() * 1.2f;
		btn_Home.y = ScreenHeight() - btn_Home.getHeight() * 1.2f;

		//replay button
		btn_Replay = new Button(new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.replay), ScreenWidth() * 0.1f), 0, 0, this, false);
		btn_Replay.x = btn_Replay.getWidth() * 0.2f;
		btn_Replay.y = ScreenHeight() - btn_Replay.getHeight() * 1.2f;

		//share on facebook
		btn_facebook = new Button(new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.facebook), ScreenWidth() * 0.2f), 0, ScreenHeight() * 0.05f, this, false);

		//sound buttons
		music_on = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.music_on), ScreenHeight() * 0.1f);
		music_off = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.music_off), ScreenHeight() * 0.1f);
		sound_off = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.sound_off), ScreenHeight() * 0.1f);
		sound_on = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.sound_on), ScreenHeight() * 0.1f);
		//music mute
		btn_music_mute = new Button(music_on, 0, 0, this, false);
		btn_music_mute.x = ScreenWidth() - btn_music_mute.getWidth() * 1.2f;
		btn_music_mute.y = btn_music_mute.getHeight() * 0.06f;
		//sound mute
		btn_sound_mute = new Button(sound_on, ScreenWidth() - btn_music_mute.getWidth() * 2.5f, btn_music_mute.getHeight() * 0.15f, this, false);

		//pause button
		play_btn_sprite = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.play_pause), ScreenHeight() * 0.08f);
		pause_btn_sprite = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.pause), ScreenHeight() * 0.08f);
		btn_pause = new Button(pause_btn_sprite, ScreenWidth() - btn_music_mute.getWidth() * 4f, btn_music_mute.getHeight() * 0.17f, this, false);

		//set world origin
		setOrigin(BOTTOM_LEFT);

		//initialise character
		bird = new Instance(new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.bird), (ScreenHeight() * 0.15f), 3, 3, 498), 50, 300, this, true);

		//initialise clouds
		cloud_sprite = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.cloud_1), ScreenHeight() * 0.45f);
		cloud_sprite2 = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.cloud_2), ScreenHeight() * 0.3f);

		//initialise buildings
		Sprite back = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.background), (ScreenWidth() * 1.01f));
		background_building_1 = new Instance(back, 0, ScreenHeight() - back.getHeight(), this, false);
		background_building_2 = new Instance(back, back.getWidth() - 2, ScreenHeight() - back.getHeight(), this, false);

		//initialise column edge
		column_edge = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.column_edge), ScreenHeight() * 0.2f);
		hole_size = bird.getHeight() * hole_size_ComparedToBird;

		//initialise score image
		score_cup = new Sprite(BitmapFactory.decodeResource(getResources(), R.drawable.score), ScreenHeight() * 0.3f);

		//initialise sound fx
		activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);

		//initialise music
		music = MediaPlayer.create(activity, R.raw.music);
		sound_score = sp.load(activity, R.raw.coin, 1);
		sound_fall = sp.load(activity, R.raw.fall, 1);
		sound_beep = sp.load(activity, R.raw.beep, 1);

	}

	@Override
	synchronized public void Step(int rand) {
		super.Step(rand);
		if (state == MENU) {

		} else if (state == GAMEPLAY) {

			//things to pause
			if (!notstarted && !pause && !game_over) {
				//bird movement
				bird.Update();
				if (-bird.speedy > -70)
					bird.rotate(-bird.speedy);

				//collision to boundary
				if (bird.y < bird.getHeight()) {
					game_over = true;
					if (sound_fall != 0 && !sound_muted)
						sp.play(sound_fall, 1, 1, 0, 0, 1);
				}
				if (bird.y > ScreenHeight()) {
					bird.y = ScreenHeight();
					bird.speedy = 0;
				}

				int temp_score = 0;
				for (int i = 0; i < column_positions.length; i++) {
					float y = column_positions[i][Y] * ScreenHeight();
					float x = ScreenX((int) column_positions[i][X] * ScreenWidth() * 0.0015f);
					float birdx = ScreenX(bird.x) + (bird.getHeight() * 0.15f);
					float birdy = ScreenY(bird.y) + (bird.getWidth() * 0.15f);
					float birdw = bird.getWidth() * 0.7f;
					float birdh = bird.getHeight() * 0.7f;
					if (physics.intersect((int) birdx, (int) birdy, (int) birdw, (int) birdh, (int) x, (int) (y + hole_size / 2), (int) column_edge.getWidth(), (int) (ScreenHeight() - (y + hole_size / 2))) || physics.intersect((int) birdx, (int) birdy, (int) birdw, (int) birdh, (int) x, 0, (int) column_edge.getWidth(), (int) ((int) y - (hole_size / 2) + column_edge.getHeight()))) {
						bird.speedy = -dpToPx(40);
					}
					if (bird.x > column_positions[i][X] * ScreenWidth() * 0.0015f) {
						temp_score++;
					}
				}
				if (temp_score > score) {
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 1);
				}
				score = temp_score;

				//move camera
				if (ScreenX(bird.x) > ScreenWidth() / 2) {
					//move cloud with camera
					for (int i = 0; i < clouds.size(); i++)
						clouds.get(i).x += ((ScreenWidth() / 2) - ScreenX(bird.x)) * 0.6 * (i + 1);
					cameraX += bird.speedx;
					background_building_1.Update();
					background_building_2.Update();
				}
				for (int i = 0; i < clouds.size(); i++)
					clouds.get(i).x += -(2 * (i + 1));

				//move background
				if (background_building_1.x < -ScreenWidth())
					background_building_1.x = ScreenWidth();
				if (background_building_2.x < -ScreenWidth())
					background_building_2.x = ScreenWidth();

			}

			//check for game over
			if (game_over)
				gameover_counter++;
			else
				gameover_counter = 0;
			if (gameover_counter > gameover_delay)
				GameOver();

			//move cloud
			for (int i = clouds.size() - 1; i >= 0; i--) {
				if (clouds.size() > i) {
					if (clouds.get(i).x < -clouds.get(i).getWidth()) {
						clouds.remove(i);
						createCloud((float) (ScreenWidth() + (Math.random() * dpToPx(300))));
					}
				}
			}
		}

	}

	@Override
	public synchronized void onAccelerometer(PointF point) {

	}

	@Override
	public synchronized void BackPressed() {
		if (state == GAMEPLAY) {
			StopMusic();
			state = MENU;
		} else if (state == HIGHSCORES) {
			state = MENU;
		} else if (state == MENU) {
			StopMusic();
			Exit();

		} else if (state == GAMEOVER) {
			highscoreManager.newScore(score, getResources().getString(R.string.Default_topscore_name));
			state = MENU;
		}
	}

	@Override
	public synchronized void onTouch(float TouchX, float TouchY, MotionEvent event) {
		//handle constant events like sound buttons
		if (event.getAction() == MotionEvent.ACTION_DOWN) {

			if (btn_sound_mute.isTouched(event)) {
				btn_sound_mute.Highlight(RED);
			}
			if (btn_music_mute.isTouched(event)) {
				btn_music_mute.Highlight(RED);
			}
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			//refresh all
			btn_music_mute.LowLight();
			btn_sound_mute.LowLight();

			if (btn_sound_mute.isTouched(event)) {
				toggleSoundFx();
			}
			if (btn_music_mute.isTouched(event)) {
				toggleMusic();
			}
		}

		if (state == MENU) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (btn_Play.isTouched(event)) {
					btn_Play.Highlight(RED);
				}
				if (btn_Highscores.isTouched(event)) {
					btn_Highscores.Highlight(RED);
				}
				if (btn_Exit.isTouched(event)) {
					btn_Exit.Highlight(RED);
				}
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				//refresh all
				btn_Play.LowLight();
				btn_Highscores.LowLight();
				btn_Exit.LowLight();

				if (btn_Play.isTouched(event)) {
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 1);
					StartGame();
				}
				if (btn_Highscores.isTouched(event)) {
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 1);
					OpenHighscores();
				}
				if (btn_Exit.isTouched(event)) {
					Exit();
				}
			}
			if (event.getAction() == MotionEvent.ACTION_MOVE) {

			}
		} else if (state == HIGHSCORES) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (btn_Home.isTouched(event)) {
					btn_Home.Highlight(RED);
				}
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				//refresh all
				btn_Home.LowLight();

				if (btn_Home.isTouched(event)) {
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 1);
					state = MENU;
				}
			}
			if (event.getAction() == MotionEvent.ACTION_MOVE) {

			}
		} else if (state == GAMEOVER) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (btn_Home.isTouched(event)) {
					btn_Home.Highlight(RED);
				}
				if (btn_facebook.isTouched(event)) {
					btn_facebook.Highlight(WHITE);
				}
				if (btn_Replay.isTouched(event)) {
					btn_Replay.Highlight(RED);
				}
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				//refresh all
				btn_Home.LowLight();
				btn_facebook.LowLight();
				btn_Replay.LowLight();
				if (btn_Home.isTouched(event)) {
					//show_enter_highscore();
					highscoreManager.newScore(score, getResources().getString(R.string.Default_topscore_name));
					state = MENU;
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 1);
				}
				if (btn_facebook.isTouched(event)) {
					//share with facebook
					highscoreManager.postToFacebook("" + score, getResources().getString(R.string.facebook_share_link), getResources().getString(R.string.facebook_share_description), getResources().getString(R.string.Error_no_facebook_app_installed));
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 1);
				}
				if (btn_Replay.isTouched(event)) {
					highscoreManager.newScore(score, getResources().getString(R.string.Default_topscore_name));
					StartGame();
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 1);

				}
			}
		} else if (state == GAMEPLAY) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (btn_pause.isTouched(event)) {
					btn_pause.Highlight(RED);
				}

			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				//start game
				if (notstarted) {
					notstarted = false;
				}
				//unpause
				if (pause) {
					unPause();
				} else {
					//during gameplay
					//move bird up
					if (!notstarted && !pause && !game_over) {
						bird.speedy = ScreenHeight() * touch_speed * 0.0015f;
					}

					btn_pause.LowLight();
					if (btn_pause.isTouched(event)) {
						togglePause();
						if (sound_beep != 0 && !sound_muted)
							sp.play(sound_beep, 1, 1, 0, 0, 1);
					}
				}

			}

		}
	}

	//..................................................Game Functions..................................................................................................................................

	public void StartGame() {
		//refresh score
		score = 0;

		//refresh bird
		bird.accelerationy = -ScreenHeight() * 0.0001f * gravity;
		bird.speedx = ScreenWidth() * 0.01f;
		bird.speedy = 0;
		bird.y = ScreenHeight() / 2;
		bird.x = dpToPx(30);
		bird.rotate(0);

		//refresh camera
		cameraY = 0;
		cameraX = 0;

		//clouds
		clouds.clear();
		createCloud((float) (Math.random() * dpToPx(300)));
		createCloud((float) (Math.random() * dpToPx(300)));

		//background buildings
		background_building_1.speedx = -dpToPx(3);
		background_building_2.speedx = -dpToPx(3);
		background_building_1.x = 0;
		background_building_2.x = background_building_1.getWidth();

		//not started
		notstarted = true;
		game_over = false;
		state = GAMEPLAY;
		PlayMusic();

		//pause off
		pause = false;
	}

	public synchronized void GameOver() {
		ad_counter++;
		if (ad_counter >= this.getResources().getInteger(R.integer.ad_shows_every_X_gameovers)) {
			openAd();
			ad_counter = 0;
		}
		
		StopMusic();
		state = GAMEOVER;
		highscoreManager.AddName_Editview(((int) (ScreenWidth() / 1.5f) < dpToPx(250)) ? ((int) (ScreenWidth() / 1.5f)) : (dpToPx(250)), getResources().getString(R.string.Highscore_hint), (int) (ScreenHeight() * 0.68f));

	}

	public void OpenHighscores() {
		state = HIGHSCORES;
		highscore_list = highscoreManager.load_localscores();
	}

	public void createCloud(float x) {
		if (Math.random() > 0.5)
			clouds.add(new Instance(cloud_sprite, x, (float) ((Math.random() * ScreenHeight() / 2) - (cloud_sprite.getHeight() / 2)), this, false));
		else
			clouds.add(new Instance(cloud_sprite2, x, (float) ((Math.random() * ScreenHeight() / 2) - (cloud_sprite.getHeight() / 2)), this, false));
	}

	public void PlayMusic() {
		if (!music_muted && state == GAMEPLAY) {
			music = MediaPlayer.create(activity, R.raw.music);
			music.start();
			music.setLooping(true);
		}
	}

	public void StopMusic() {
		if (music != null)
			music.stop();
	}

	public void toggleMusic() {
		if (music_muted) {

			music_muted = false;
			btn_music_mute.sprite = music_on;
			if (!pause) {
				PlayMusic();
			}
		} else {
			music_muted = true;
			btn_music_mute.sprite = music_off;
			StopMusic();
		}
	}

	public void toggleSoundFx() {
		if (sound_muted) {
			sound_muted = false;
			btn_sound_mute.sprite = sound_on;
		} else {
			sound_muted = true;
			btn_sound_mute.sprite = sound_off;
		}
	}

	public void pause() {
		if (state == GAMEPLAY) {
			pause = true;
			StopMusic();
			btn_pause.sprite = play_btn_sprite;
		}
	}

	public void unPause() {
		pause = false;
		btn_pause.sprite = pause_btn_sprite;
		if (!music_muted)
			PlayMusic();

	}

	public void togglePause() {
		if (state == GAMEPLAY) {
			if (pause)
				unPause();
			else
				pause();

		}
	}

	//...................................................Rendering of screen............................................................................................................................
	@Override
	public void Draw(Canvas canvas) {
		//draw background
		renderBackground(canvas);

		if (state == MENU) {
			//draw clouds
			cloud_sprite2.draw(canvas, dpToPx(10), dpToPx(50));
			cloud_sprite.draw(canvas, ScreenWidth() - (cloud_sprite.getWidth() * 0.75f), ScreenHeight() - (cloud_sprite.getHeight() * 0.8f));

			canvas.drawText(getResources().getString(R.string.app_name), (ScreenWidth() / 2) - (Title_Paint.measureText(getResources().getString(R.string.app_name)) / 2), (float) (ScreenHeight() * 0.25), Title_Paint);
			btn_Play.draw(canvas);
			btn_Highscores.draw(canvas);
			btn_Exit.draw(canvas);

		} else if (state == GAMEPLAY) {
			//draw clouds
			for (int i = 0; i < clouds.size(); i++) {
				clouds.get(i).draw(canvas);
			}

			//draw back
			background_building_1.draw(canvas);
			background_building_2.draw(canvas);

			//draw floor
			canvas.drawRect(0, ScreenHeight() - (ScreenHeight() * 0.04f), ScreenWidth(), ScreenHeight(), Black_shader);

			//draw columns
			for (int i = 0; i < column_positions.length; i++) {
				float y = column_positions[i][Y] * ScreenHeight();
				float x = ScreenX((int) column_positions[i][X] * ScreenWidth() * 0.0015f);

				//top
				canvas.drawRect(x, 0, x + column_edge.getWidth(), y - hole_size / 2, Black_shader);
				column_edge.draw(canvas, x, y - hole_size / 2);
				//bottom
				canvas.drawRect(x, y + hole_size / 2, x + column_edge.getWidth(), ScreenHeight(), Black_shader);
				column_edge.draw(canvas, x, y + hole_size / 2);

			}

			//draw bird
			bird.draw(canvas);

			//draw score
			canvas.drawText("" + score, (ScreenWidth() * 0.5f) - (Title_Paint.measureText("" + score) / 2), (float) (ScreenHeight() * 0.35f), Score_Paint);

			//before game starts
			if (notstarted) {
				canvas.drawText(getResources().getString(R.string.Tap_to_start), (ScreenWidth() / 2) - (Instruction_Paint.measureText(getResources().getString(R.string.Tap_to_start)) / 2), (float) (ScreenHeight() * 0.5), Instruction_Paint);
			} else if (pause) {
				canvas.drawText(getResources().getString(R.string.Paused), (ScreenWidth() / 2) - (Instruction_Paint.measureText(getResources().getString(R.string.Paused)) / 2), (float) (ScreenHeight() * 0.5), Instruction_Paint);
			}

			//pause button
			btn_pause.draw(canvas);

		} else if (state == HIGHSCORES) {
			//draw clouds
			cloud_sprite2.draw(canvas, dpToPx(10), dpToPx(50));
			cloud_sprite.draw(canvas, ScreenWidth() - (cloud_sprite.getWidth() * 0.75f), ScreenHeight() - (cloud_sprite.getHeight() * 0.8f));

			canvas.drawText(getResources().getString(R.string.Highscores), (ScreenWidth() / 2) - (Title_Paint.measureText(getResources().getString(R.string.Highscores)) / 2), (float) (ScreenHeight() * 0.25), Title_Paint);

			if (highscore_list != null) {
				//hiscores
				for (int i = 0; i < highscore_list.length; i++) {
					canvas.drawText(highscore_list[i].hiscorename, (ScreenWidth() / 2) - (ScreenWidth() / 4), (ScreenHeight() * 0.35f) + (i * SubTitle_Paint.getTextSize() * 1.5f), SubTitle_Paint);
					canvas.drawText("" + highscore_list[i].highscore, (ScreenWidth() / 2) + (ScreenWidth() / 6), (ScreenHeight() * 0.35f) + (i * SubTitle_Paint.getTextSize() * 1.5f), SubTitle_Paint);
				}
			}

			btn_Home.draw(canvas);
		} else if (state == GAMEOVER) {
			//draw clouds
			cloud_sprite2.draw(canvas, dpToPx(10), dpToPx(50));
			cloud_sprite.draw(canvas, ScreenWidth() - (cloud_sprite.getWidth() * 0.75f), ScreenHeight() - (cloud_sprite.getHeight() * 0.8f));

			canvas.drawText(getResources().getString(R.string.game_over), (ScreenWidth() / 2) - (Title_Paint.measureText(getResources().getString(R.string.game_over)) / 2), (float) (ScreenHeight() * 0.25), Title_Paint);

			//score_cup.draw(canvas, (ScreenWidth() / 2) - (score_cup.getWidth() / 2), (float) (ScreenHeight() * 0.30));

			canvas.drawText("" + score, (ScreenWidth() / 2) - (Score_Paint.measureText("" + score) / 2), (float) (ScreenHeight() * 0.45), Score_Paint);

			canvas.drawText(getResources().getString(R.string.Enter_highscore_comment), (ScreenWidth() / 2) - (SubTitle_Paint.measureText(getResources().getString(R.string.Enter_highscore_comment)) / 2), (float) (ScreenHeight() * 0.65), SubTitle_Paint);

			btn_facebook.draw(canvas);
			btn_Home.draw(canvas);
			btn_Replay.draw(canvas);

		}
		//draw sound buttons
		btn_sound_mute.draw(canvas);
		btn_music_mute.draw(canvas);

		//physics.drawDebug(canvas);
		super.Draw(canvas);
	}

	//Rendering of background
	public void renderBackground(Canvas canvas) {

		//TODO: you may wish to change background colors from here
		canvas.drawColor(Color.rgb(153, 204, 255));
		//TODO: unhighlight the code below to make the background a gradient
		//Paint background_paint = new Paint();
		//background_paint.setShader(new LinearGradient(ScreenWidth() / 2, 0, ScreenWidth() / 2, ScreenHeight(), Color.argb(255, 173, 230, 255), Color.argb(255, 70, 151, 250), Shader.TileMode.CLAMP));
		//canvas.drawRect(0, 0, ScreenWidth(), ScreenHeight(), background_paint);

		background_shader.setARGB(255, 205, 229, 240);
		int radius = DrawBackgroundCloud(canvas, (int) (ScreenHeight() / 2.5), 10);
		canvas.drawRect(0, (float) ((ScreenHeight() / 2.8) + radius * 1.5), ScreenWidth(), ScreenHeight(), background_shader);

		background_shader.setARGB(255, 108, 181, 213);
		radius = DrawBackgroundCloud(canvas, (int) (ScreenHeight() / 1.7), 7);
		canvas.drawRect(0, (float) ((ScreenHeight() / 2.3) + radius * 1.5), ScreenWidth(), ScreenHeight(), background_shader);

	}

	public int DrawBackgroundCloud(Canvas canvas, int y, int circles) {
		int radius = (int) (ScreenWidth() / (circles * 1.3));
		for (int i = 0; i < circles; i++) {
			canvas.drawCircle((float) (i * radius * 1.5), (float) (y + radius + (Math.sin(i * circles * y) * radius * 0.35f)), radius, background_shader);
		}
		return radius;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		highscoreManager.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		super.onResume();
		highscoreManager.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		highscoreManager.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();
		pause();
		highscoreManager.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		highscoreManager.onDestroy();
	}
}
