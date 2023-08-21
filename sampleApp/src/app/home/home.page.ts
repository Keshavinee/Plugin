import { Component } from '@angular/core';
import { IonicModule, AlertController } from '@ionic/angular';

import 'sample-plugin';
import { Plugins } from '@capacitor/core';
const { ExamplePlugin } = Plugins;

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
  standalone: true,
  imports: [IonicModule],
})
export class HomePage {
  private startTime: number;
  private timerInterval: any; // Interval reference
  private isTaskRunning: boolean;


  constructor(private alertController: AlertController) {
    this.startTime = 0;
    this.isTaskRunning = false;
  }

  async startWork() {
	if (this.isTaskRunning) {
    		return;
  	}

	this.isTaskRunning = true;
	this.startTime = Date.now();
  	this.timerInterval = setInterval(() => {
		if (this.isTaskRunning) {
    			this.updateTimer();
  		}
    		
  	}, 1000); // Update the timer every second

  	try {
    		const result = await ExamplePlugin['startWork']();
    		this.updateTimer(); // Ensure the timer displays the final time
		clearInterval(this.timerInterval);
    		this.showDialog(result.workId+':Work Completed', 'Background task completed successfully.');
  	} catch (error) {
		this.updateTimer(); // Ensure the timer displays the final time
    		clearInterval(this.timerInterval);
    		this.showDialog('Error', 'Background task encountered an error.');
  	} finally {
    		this.isTaskRunning = false; // Mark the task as not running
  	}
  }

  updateTimer() {
    const timerElement = document.getElementById('timer');
    if (timerElement) {
      const currentTime = new Date().getTime();
      const elapsedTime = currentTime - this.startTime;
      const formattedTime = this.formatTime(elapsedTime);
      timerElement.textContent = formattedTime;
    }
  }

  formatTime(elapsedTime: number): string {
    const milliseconds = elapsedTime % 1000;
    const totalSeconds = Math.floor(elapsedTime / 1000);
    const seconds = totalSeconds % 60;
    const totalMinutes = Math.floor(totalSeconds / 60);
    const minutes = totalMinutes % 60;
    const hours = Math.floor(totalMinutes / 60);

    const formattedHours = hours.toString().padStart(2, '0');
    const formattedMinutes = minutes.toString().padStart(2, '0');
    const formattedSeconds = seconds.toString().padStart(2, '0');
    const formattedMilliseconds = milliseconds.toString().padStart(3, '0');

    return `${formattedHours}:${formattedMinutes}:${formattedSeconds}.${formattedMilliseconds}`;
  }

  async displayStatus() {
    const stat= await ExamplePlugin['displayStatus']();
    if (stat.value === 0) {
      this.showDialog("Message", "Background task not started");
    }
    if (stat.value === 99) {
      this.showDialog("Message", "Background task is running");
    }
    if (stat.value === 1) {
      this.showDialog("Success", "Work is completed successfully!!");
    }
    if (stat.value === -1) {
      this.showDialog("Error", "Work failed due to network error!!");
    }
  }

  async showDialog(title: string, message: string) {
    const alert = await this.alertController.create({
      header: title,
      message: message,
      buttons: ['OK']
    });

    await alert.present();
  }

  async getFileCount() {
    try {
      const result = await ExamplePlugin['getFileCount']();
      const count = result.value;
      console.log('Total files downloaded:', count);
      
      const countElement = document.getElementById('count');
      if (countElement){
	countElement.textContent = count;
      }
      
    } catch (error) {
      console.error('Error retrieving file count:', error);
    }
  }

  async stopWork() {
    await ExamplePlugin['stopWork']();
    this.isTaskRunning = false;
    this.showDialog("Message", "Background task stopped.");
    const timerElement = document.getElementById('timer');
    if (timerElement) {
      timerElement.textContent = '00:00:00';
    }
  }
}
