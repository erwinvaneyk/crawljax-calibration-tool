-- phpMyAdmin SQL Dump
-- version 2.11.11
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jun 13, 2014 at 03:59 PM
-- Server version: 5.0.95
-- PHP Version: 5.1.6

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Database: `crawljaxsuite`
--

-- --------------------------------------------------------

--
-- Table structure for table `benchmarkSite`
--

CREATE TABLE IF NOT EXISTS `benchmarkSite` (
  `websiteId` int(11) NOT NULL,
  `stateIdFirst` varchar(80) NOT NULL,
  `stateIdSecond` varchar(80) default NULL,
  UNIQUE KEY `uniqueness` (`websiteId`,`stateIdFirst`,`stateIdSecond`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `configuration`
--

CREATE TABLE IF NOT EXISTS `configuration` (
  `section` varchar(32) NOT NULL default 'common',
  `key` varchar(32) NOT NULL,
  `value` varchar(64) default NULL,
  `depth` int(11) NOT NULL default '0',
  UNIQUE KEY `key` (`key`,`section`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `DomResults`
--

CREATE TABLE IF NOT EXISTS `DomResults` (
  `websiteResult_id` int(11) NOT NULL,
  `stateId` varchar(100) NOT NULL,
  `dom` longtext,
  `strippedDom` longtext,
  `strippedDomHash` varchar(100) default NULL,
  `screenshot` mediumblob,
  UNIQUE KEY `UniqueId` (`websiteResult_id`,`stateId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `WebsiteResults`
--

CREATE TABLE IF NOT EXISTS `WebsiteResults` (
  `id` int(11) NOT NULL auto_increment,
  `workTask_id` int(11) NOT NULL,
  `jsonResults` longtext,
  `duration` float default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2839 ;

-- --------------------------------------------------------

--
-- Table structure for table `workload`
--

CREATE TABLE IF NOT EXISTS `workload` (
  `id` int(11) NOT NULL auto_increment,
  `url` varchar(255) NOT NULL,
  `worker` varchar(255) NOT NULL,
  `crawled` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=10678 ;
