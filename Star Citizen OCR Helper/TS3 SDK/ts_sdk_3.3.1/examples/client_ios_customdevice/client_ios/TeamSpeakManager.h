//
//  TeamSpeakManager.h
//  client_ios
//
//  Copyright (c) 2017 TeamSpeak-Systems
//

#import <Foundation/Foundation.h>

#import "AudioIO.h"
#import "teamspeak/public_definitions.h"

@class TeamSpeakManager;

@protocol TeamSpeakManagerDelegate

- (void)teamSpeakManager:(TeamSpeakManager *)manager connectStatusChanged:(int)newStatus;
- (void)teamSpeakManager:(TeamSpeakManager *)manager clientID:(int)clientID talkStatusChanged:(BOOL)talking;
- (void)teamSpeakManager:(TeamSpeakManager *)manager onConnectionError:(int)error;

@end

@interface TeamSpeakManager : NSObject <AudioIODelegate>

+ (instancetype)sharedManager;

- (NSString *)createIdentity;

- (void)connectToIPAddress:(NSString *)IPAddress port:(int)port password:(NSString *)password;
- (void)disconnect;

@property (nonatomic, readonly) NSString *clientLibVersion;

@property (nonatomic, readonly) int selfClientID;
@property (nonatomic, readonly) BOOL isConnected;
@property (nonatomic, readonly) BOOL isConnecting;
@property (nonatomic, readonly) BOOL isDisconnected;
@property (nonatomic, readonly) int  connectStatus;
@property (nonatomic, readonly) NSString *connectStatusString;

@property (nonatomic, readonly) AudioIO *audioIO;
@property (nonatomic, strong)  NSString *identity;
@property (nonatomic, strong)  NSString *nickname;

@property (nonatomic, weak) id<TeamSpeakManagerDelegate> delegate;

@end
