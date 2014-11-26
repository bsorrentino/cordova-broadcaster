//
//  StoryboardViewController.m
//  broadcasterApp
//
//  Created by softphone on 24/11/14.
//
//

#import "StoryboardViewController.h"

@interface StoryboardViewController ()
- (IBAction)sendNativeEvent:(id)sender;

@end

@implementation StoryboardViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

- (IBAction)sendNativeEvent:(id)sender {
    
    [[NSNotificationCenter defaultCenter] postNotificationName:@"didShow"
                                                        object:nil
                                                      userInfo:@{ @"data":@"test"}];
}
@end
