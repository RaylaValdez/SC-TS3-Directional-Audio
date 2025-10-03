//
//  ViewController.m
//  client_ios
//
//  Copyright (c) 2017 TeamSpeak-Systems
//

#import "ViewController.h"

@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    
    [TeamSpeakManager sharedManager].delegate = self;
    
    self.libVersionLabel.text = [TeamSpeakManager sharedManager].clientLibVersion;
    self.connectStatusLabel.text = [TeamSpeakManager sharedManager].connectStatusString;
    
    self.hostField.text = @"127.0.0.1";
    self.passwordField.text = @"secret";
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (IBAction)onConnectButton:(id)sender {
    [self.hostField resignFirstResponder];
    [self.passwordField resignFirstResponder];
    
    TeamSpeakManager *manager = [TeamSpeakManager sharedManager];
    if (manager.isDisconnected)
    {
        [manager connectToIPAddress:self.hostField.text port:9987 password:self.passwordField.text];
    }
    else
    {
        [manager disconnect];
    }
}

- (void)teamSpeakManager:(TeamSpeakManager *)manager connectStatusChanged:(int)newStatus
{
    self.connectStatusLabel.text = manager.connectStatusString;
    
    if (manager.isDisconnected)
    {
        [self.connectButton setTitle:@"Connect" forState:UIControlStateNormal];
        self.hostField.enabled = YES;
        self.passwordField.enabled = YES;
        
        // Reset the connection info labels with an empty TeamSpeakConnectionInfo object
        [self teamSpeakManager:manager onSelfConnectionInfoUpdate:[[TeamSpeakConnectionInfo alloc] init]];
    }
    else
    {
        [self.connectButton setTitle:@"Disconnect" forState:UIControlStateNormal];
        self.hostField.enabled = NO;
        self.passwordField.enabled = NO;
    }
}

- (void)teamSpeakManager:(TeamSpeakManager *)manager clientID:(int)clientID talkStatusChanged:(BOOL)talking
{
    if (clientID == manager.selfClientID)
    {
        if (talking)
        {
            self.talkStatusLabel.text = @"TALKING";
            self.talkStatusLabel.backgroundColor = [UIColor blueColor];
        }
        else
        {
            self.talkStatusLabel.text = @"NOT TALKING";
            self.talkStatusLabel.backgroundColor = [UIColor clearColor];
        }
    }
}

- (void)teamSpeakManager:(TeamSpeakManager *)manager onConnectionError:(int)error
{
    UIAlertController * alert = [UIAlertController
                                 alertControllerWithTitle:@"Connection error"
                                 message:[NSString stringWithFormat:@"Error code: %04x", error]
                                 preferredStyle:UIAlertControllerStyleAlert];
    
    
    
    UIAlertAction* okButton = [UIAlertAction
                               actionWithTitle:@"OK"
                               style:UIAlertActionStyleDefault
                               handler:nil];
    [alert addAction:okButton];
    
    [self presentViewController:alert animated:YES completion:nil];
}

- (void)teamSpeakManager:(TeamSpeakManager *)manager onSelfConnectionInfoUpdate:(TeamSpeakConnectionInfo *)info
{
    if (info.pingCalculating) {
        self.pingLabel.text = @"<calculating>";
        
    }
    else {
        self.pingLabel.text = [NSString stringWithFormat:@"%i ms + %.2f", (int)roundf(info.ping), info.pingDeviation];
    }
    self.packetLossInLabel.text = [NSString stringWithFormat:@"%i %%", (int)roundf(info.incomingPacketLoss * 100)];
    self.packetLossOutLabel.text = [NSString stringWithFormat:@"%i %%", (int)roundf(info.outgoingPacketLoss * 100)];
}

@end
