//
//  ViewController.h
//  client_ios
//
//  Copyright (c) 2017 TeamSpeak-Systems
//

#import <UIKit/UIKit.h>
#import "TeamSpeakManager.h"

@interface ViewController : UIViewController <TeamSpeakManagerDelegate>

@property (weak, nonatomic) IBOutlet UITextField *hostField;
@property (weak, nonatomic) IBOutlet UITextField *passwordField;
@property (weak, nonatomic) IBOutlet UIButton *connectButton;
@property (weak, nonatomic) IBOutlet UILabel *connectStatusLabel;
@property (weak, nonatomic) IBOutlet UILabel *talkStatusLabel;
@property (weak, nonatomic) IBOutlet UILabel *libVersionLabel;

- (IBAction)onConnectButton:(id)sender;

@end

