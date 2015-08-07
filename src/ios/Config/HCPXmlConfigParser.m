//
//  HCPXmlConfigParser.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 07.08.15.
//
//

#import "HCPXmlConfigParser.h"

@interface HCPXmlConfigParser() <NSXMLParserDelegate> {
    BOOL _didParseCHCPBlock;
    BOOL _isInCHCPBlock;
    BOOL _isInLocalDevBlock;
    BOOL _isInJsCodeBlock;
    
    NSMutableString *_jsCodeString;
}

@property (nonatomic, retain) NSURL *configXmlFileURL;

@end

static NSString *const MAIN_TAG = @"chcp";

static NSString *const CONFIG_FILE_TAG = @"config-file";
static NSString *const CONFIG_FILE_URL_ATTRIBUTE = @"url";

static NSString *const LOCAL_DEVELOPMENT_TAG = @"local-development";
static NSString *const LOCAL_DEVELOPMENT_ENABLED_ATTRIBUTE = @"enabled";
static NSString *const INJECT_JS_CODE_TAG = @"inject-js-code";
static NSString *const INJECT_JS_SCRIPT_TAG = @"inject-js-script";
static NSString *const INJECT_JS_SCRIPT_PATH_ATTRIBUTE = @"path";

@implementation HCPXmlConfigParser

- (instancetype)init {
    self = [super init];
    if (self) {
        NSString *filePath = [[NSBundle mainBundle] pathForResource:@"config" ofType:@"xml"];
        self.configXmlFileURL = [NSURL fileURLWithPath:filePath];
    }
    
    return self;
}

+ (HCPXmlConfig *)parse {
    HCPXmlConfigParser *parser = [[HCPXmlConfigParser alloc] init];
    
    return [parser parseConfig];
}

- (HCPXmlConfig *)parseConfig {
    NSXMLParser *configParser = [[NSXMLParser alloc] initWithContentsOfURL:self.configXmlFileURL];
    if (configParser == nil) {
        NSLog(@"Failed to initialize XML parser.");
        return nil;
    }
    
    [configParser setDelegate:self];
    [configParser parse];
    
    return nil;
}

#pragma mark NSXMLParserDelegate implementation

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict {
    if (_didParseCHCPBlock) {
        return;
    }
    
    if ([elementName isEqualToString:MAIN_TAG]) {
        _isInCHCPBlock = YES;
        return;
    }
    
    if (!_isInCHCPBlock) {
        return;
    }
    
    if ([elementName isEqualToString:CONFIG_FILE_TAG]) {
        
    }
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName {
    if (_didParseCHCPBlock || !_isInCHCPBlock) {
        return;
    }
    
    if ([elementName isEqualToString:MAIN_TAG]) {
        _didParseCHCPBlock = YES;
        return;
    }
    
    
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string {
    if (_didParseCHCPBlock || !_isInCHCPBlock) {
        return;
    }
    
    
}

@end
