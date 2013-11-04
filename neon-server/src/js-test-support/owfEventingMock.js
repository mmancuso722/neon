/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 */

neon.mock.channelRegistry = {};

neon.mock.Channel = function() {
    this.callbacks = [];
}

OWF.Util = {
    isRunningInOWF: function(){
        return true;
    }
};

OWF.Eventing.subscribe = function(channelName, callback) {
    if ( !(channelName in neon.mock.channelRegistry) ) {
        neon.mock.channelRegistry[channelName] = new neon.mock.Channel();
    }
    neon.mock.channelRegistry[channelName].callbacks.push(callback)
};

OWF.Eventing.publish = function(channelName, message) {
    var channel = neon.mock.channelRegistry[channelName];
    channel.callbacks.forEach(function(callback) {
        callback.call(null, 'owfEventingMockSender', message);
    });

};

neon.mock.clearChannels = function() {
    neon.mock.channelRegistry = {};
};